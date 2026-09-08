package com.leilao.leilao_games.controller;

import com.leilao.leilao_games.dto.LanceDTO;
import com.leilao.leilao_games.dto.RegistrarLanceRequestDTO;
import com.leilao.leilao_games.model.Lance;
import com.leilao.leilao_games.model.Produto;
import com.leilao.leilao_games.model.Usuario;
import com.leilao.leilao_games.service.LanceService;
import com.leilao.leilao_games.service.NotificacaoService;
import com.leilao.leilao_games.service.ProdutoService;
import com.leilao.leilao_games.service.TempoRealService;
import com.leilao.leilao_games.service.UsuarioService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@CrossOrigin(
        origins = "${app.cors.allowed-origin}",
        allowCredentials = "true"
)
@RestController
@RequestMapping("/api/produtos/{produtoId}/lances")
@RequiredArgsConstructor
public class ApiLanceController {

    private final LanceService lanceService;
    private final ProdutoService produtoService;
    private final UsuarioService usuarioService;
    private final NotificacaoService notificacaoService;
    private final TempoRealService tempoRealService;

    @GetMapping
    public ResponseEntity<List<LanceDTO>> listarLances(
            @PathVariable Long produtoId) {

        Produto produto = produtoService.buscarPorId(produtoId);

        if (produto == null) {
            return ResponseEntity.notFound().build();
        }

        List<LanceDTO> lances = lanceService
                .buscarPorProduto(produtoId)
                .stream()
                .map(LanceDTO::de)
                .toList();

        return ResponseEntity.ok(lances);
    }

    @PostMapping
    public ResponseEntity<?> registrarLance(
            @PathVariable Long produtoId,
            @RequestBody RegistrarLanceRequestDTO dados,
            HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session == null) {
            return naoAutenticado();
        }

        Usuario usuarioSessao = (Usuario) session.getAttribute(
                "usuarioLogado"
        );

        if (usuarioSessao == null) {
            return naoAutenticado();
        }

        Usuario usuario = usuarioService.buscarPorId(
                usuarioSessao.getId()
        );

        if (usuario == null) {
            session.invalidate();

            return naoAutenticado();
        }

        if (dados == null || dados.valor() == null) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "erro",
                            "O valor do lance é obrigatório."
                    ));
        }

        LanceService.Registro registro = lanceService.registrar(
                produtoId,
                dados.valor(),
                usuario
        );

        if (registro.resultado()
                != LanceService.Resultado.SUCESSO) {

            return erroDoRegistro(registro.resultado());
        }

        Produto produto = registro.produto();
        Lance lance = registro.lance();
        Usuario usuarioSuperado = registro.usuarioSuperado();

        notificacaoService.criar(
                produto.getUsuario(),
                "LANCE",
                usuario.getNome()
                        + " deu um lance de R$ "
                        + String.format("%.2f", dados.valor())
                        + " no produto "
                        + produto.getNome()
                        + ".",
                "/produto/" + produtoId
        );

        if (usuarioSuperado != null
                && usuarioSuperado.getId() != null
                && !usuarioSuperado.getId()
                        .equals(usuario.getId())) {

            notificacaoService.criar(
                    usuarioSuperado,
                    "LANCE_SUPERADO",
                    "Seu lance no produto \""
                            + produto.getNome()
                            + "\" foi superado por um novo lance de R$ "
                            + String.format("%.2f", dados.valor())
                            + ".",
                    "/produto/" + produtoId
            );
        }

        tempoRealService.enviarParaProduto(
                produtoId,
                "lance",
                Map.of(
                        "produtoId", produtoId,
                        "valor", dados.valor(),
                        "usuario", usuario.getNome(),
                        "dataHora", lance.getDataHora().format(
                                DateTimeFormatter.ofPattern(
                                        "dd/MM/yyyy HH:mm"
                                )
                        )
                )
        );

        return ResponseEntity.ok(
                LanceDTO.de(lance)
        );
    }

    private ResponseEntity<Map<String, String>> naoAutenticado() {
        return ResponseEntity
                .status(401)
                .body(Map.of(
                        "erro",
                        "Usuário não autenticado."
                ));
    }

    private ResponseEntity<Map<String, String>> erroDoRegistro(
            LanceService.Resultado resultado) {

        switch (resultado) {

            case PRODUTO_INEXISTENTE:
                return ResponseEntity
                        .status(404)
                        .body(Map.of(
                                "erro",
                                "Produto não encontrado."
                        ));

            case ENCERRADO:
                return ResponseEntity
                        .status(409)
                        .body(Map.of(
                                "erro",
                                "Este leilão já foi encerrado."
                        ));

            case VENDEDOR:
                return ResponseEntity
                        .status(403)
                        .body(Map.of(
                                "erro",
                                "O vendedor não pode dar lance no próprio produto."
                        ));

            case VALOR_INICIAL:
                return ResponseEntity
                        .badRequest()
                        .body(Map.of(
                                "erro",
                                "O lance é menor que o valor inicial."
                        ));

            case LANCE_MENOR:
                return ResponseEntity
                        .badRequest()
                        .body(Map.of(
                                "erro",
                                "O lance deve ser maior que o lance atual."
                        ));

            case VALOR_INVALIDO:
                return ResponseEntity
                        .badRequest()
                        .body(Map.of(
                                "erro",
                                "O valor do lance é inválido."
                        ));

            default:
                return ResponseEntity
                        .badRequest()
                        .body(Map.of(
                                "erro",
                                "Não foi possível registrar o lance."
                        ));
        }
    }
}