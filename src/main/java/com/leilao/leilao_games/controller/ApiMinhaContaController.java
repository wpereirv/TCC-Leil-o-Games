package com.leilao.leilao_games.controller;

import com.leilao.leilao_games.dto.MeuLanceDTO;
import com.leilao.leilao_games.dto.NegociacaoDTO;
import com.leilao.leilao_games.dto.ProdutoResumoDTO;
import com.leilao.leilao_games.model.Produto;
import com.leilao.leilao_games.model.Usuario;
import com.leilao.leilao_games.service.AvaliacaoService;
import com.leilao.leilao_games.service.FavoritoService;
import com.leilao.leilao_games.service.LanceService;
import com.leilao.leilao_games.service.ProdutoService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@CrossOrigin(
        origins = "${app.cors.allowed-origin}",
        allowCredentials = "true"
)
@RestController
@RequestMapping("/api/minha-conta")
@RequiredArgsConstructor
public class ApiMinhaContaController {

    private final ProdutoService produtoService;
    private final LanceService lanceService;
    private final FavoritoService favoritoService;
    private final AvaliacaoService avaliacaoService;

    @GetMapping("/anuncios")
    public ResponseEntity<?> listarAnuncios(
            HttpServletRequest request) {

        Usuario usuario = buscarUsuarioLogado(request);

        if (usuario == null) {
            return naoAutenticado();
        }

        List<ProdutoResumoDTO> anuncios =
                produtoService.buscarPorUsuario(usuario.getId())
                        .stream()
                        .map(ProdutoResumoDTO::de)
                        .toList();

        return ResponseEntity.ok(anuncios);
    }

    @GetMapping("/lances")
    public ResponseEntity<?> listarLances(
            HttpServletRequest request) {

        Usuario usuario = buscarUsuarioLogado(request);

        if (usuario == null) {
            return naoAutenticado();
        }

        List<MeuLanceDTO> lances =
                lanceService.buscarPorUsuario(usuario.getId())
                        .stream()
                        .map(MeuLanceDTO::de)
                        .toList();

        return ResponseEntity.ok(lances);
    }

    @GetMapping("/ganhos")
    public ResponseEntity<?> listarLeiloesGanhos(
            HttpServletRequest request) {

        Usuario usuario = buscarUsuarioLogado(request);

        if (usuario == null) {
            return naoAutenticado();
        }

        List<ProdutoResumoDTO> ganhos =
                produtoService.buscarPorComprador(usuario.getId())
                        .stream()
                        .map(ProdutoResumoDTO::de)
                        .toList();

        return ResponseEntity.ok(ganhos);
    }

    @GetMapping("/negociacoes")
    public ResponseEntity<?> listarNegociacoes(
            HttpServletRequest request) {

        Usuario usuario = buscarUsuarioLogado(request);

        if (usuario == null) {
            return naoAutenticado();
        }

        List<NegociacaoDTO> negociacoes =
                produtoService.buscarNegociacoes(usuario.getId())
                        .stream()
                        .map(produto ->
                                NegociacaoDTO.de(
                                        produto,
                                        usuario.getId()
                                )
                        )
                        .toList();

        return ResponseEntity.ok(negociacoes);
    }

    @DeleteMapping("/anuncios/{produtoId}")
    public ResponseEntity<?> excluirAnuncio(
            @PathVariable Long produtoId,
            HttpServletRequest request) {

        Usuario usuario = buscarUsuarioLogado(request);

        if (usuario == null) {
            return naoAutenticado();
        }

        Produto produto =
                produtoService.buscarPorId(produtoId);

        if (produto == null) {
            return ResponseEntity.status(404).body(Map.of(
                    "erro",
                    "Produto não encontrado."
            ));
        }

        if (produto.getUsuario() == null
                || !produto.getUsuario().getId()
                        .equals(usuario.getId())) {

            return ResponseEntity.status(403).body(Map.of(
                    "erro",
                    "Você não pode excluir este anúncio."
            ));
        }

        if (!lanceService.buscarPorProduto(produtoId)
                .isEmpty()) {

            return ResponseEntity.status(409).body(Map.of(
                    "erro",
                    "Este anúncio não pode ser excluído porque possui lances."
            ));
        }

        try {
            favoritoService.removerPorProduto(produtoId);
            avaliacaoService.removerPorProduto(produtoId);
            produtoService.excluir(produtoId);

            return ResponseEntity.ok(Map.of(
                    "mensagem",
                    "Anúncio excluído com sucesso."
            ));

        } catch (Exception erro) {
            return ResponseEntity.status(409).body(Map.of(
                    "erro",
                    "O anúncio possui informações vinculadas e não pode ser excluído."
            ));
        }
    }

    @PostMapping("/negociacoes/{produtoId}/pagamento")
    public ResponseEntity<?> confirmarPagamento(
            @PathVariable Long produtoId,
            HttpServletRequest request) {

        Usuario usuario = buscarUsuarioLogado(request);

        if (usuario == null) {
            return naoAutenticado();
        }

        ProdutoService.ResultadoNegociacao resultado =
                produtoService.confirmarPagamento(
                        produtoId,
                        usuario.getId()
                );

        return responderNegociacao(resultado);
    }

    @PostMapping("/negociacoes/{produtoId}/envio")
    public ResponseEntity<?> informarEnvio(
            @PathVariable Long produtoId,
            @RequestBody(required = false)
            Map<String, String> dados,
            HttpServletRequest request) {

        Usuario usuario = buscarUsuarioLogado(request);

        if (usuario == null) {
            return naoAutenticado();
        }

        String codigoRastreio = dados != null
                ? dados.get("codigoRastreio")
                : null;

        ProdutoService.ResultadoNegociacao resultado =
                produtoService.informarEnvio(
                        produtoId,
                        usuario.getId(),
                        codigoRastreio
                );

        return responderNegociacao(resultado);
    }

    @PostMapping("/negociacoes/{produtoId}/recebimento")
    public ResponseEntity<?> confirmarRecebimento(
            @PathVariable Long produtoId,
            HttpServletRequest request) {

        Usuario usuario = buscarUsuarioLogado(request);

        if (usuario == null) {
            return naoAutenticado();
        }

        ProdutoService.ResultadoNegociacao resultado =
                produtoService.confirmarRecebimento(
                        produtoId,
                        usuario.getId()
                );

        return responderNegociacao(resultado);
    }

    private ResponseEntity<?> responderNegociacao(
            ProdutoService.ResultadoNegociacao resultado) {

        if (resultado
                == ProdutoService.ResultadoNegociacao.SUCESSO) {

            return ResponseEntity.ok(Map.of(
                    "mensagem",
                    "Negociação atualizada com sucesso."
            ));
        }

        String mensagem = switch (resultado) {
            case NAO_ENCONTRADA ->
                    "Negociação não encontrada.";
            case NAO_AUTORIZADO ->
                    "Você não possui permissão para esta ação.";
            case STATUS_INVALIDO ->
                    "Esta ação não está disponível no status atual.";
            case CODIGO_INVALIDO ->
                    "Informe um código de rastreio válido.";
            default ->
                    "Não foi possível atualizar a negociação.";
        };

        return ResponseEntity.status(409).body(Map.of(
                "erro",
                mensagem
        ));
    }

    private Usuario buscarUsuarioLogado(
            HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session == null) {
            return null;
        }

        return (Usuario) session.getAttribute(
                "usuarioLogado"
        );
    }

    private ResponseEntity<?> naoAutenticado() {
        return ResponseEntity.status(401).body(Map.of(
                "erro",
                "Usuário não autenticado."
        ));
    }
}