package com.leilao.leilao_games.controller;

import com.leilao.leilao_games.dto.ConversaResumoDTO;
import com.leilao.leilao_games.dto.MensagemDTO;
import com.leilao.leilao_games.dto.MensagemRequestDTO;
import com.leilao.leilao_games.model.Conversa;
import com.leilao.leilao_games.model.Mensagem;
import com.leilao.leilao_games.model.Produto;
import com.leilao.leilao_games.model.Usuario;
import com.leilao.leilao_games.service.ConversaService;
import com.leilao.leilao_games.service.MensagemService;
import com.leilao.leilao_games.service.NotificacaoService;
import com.leilao.leilao_games.service.ProdutoService;
import com.leilao.leilao_games.service.TempoRealService;

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

import java.util.List;
import java.util.Map;

@CrossOrigin(
        origins = "${app.cors.allowed-origin}",
        allowCredentials = "true"
)
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ApiChatController {

    private final ConversaService conversaService;
    private final MensagemService mensagemService;
    private final ProdutoService produtoService;
    private final NotificacaoService notificacaoService;
    private final TempoRealService tempoRealService;

    @GetMapping
    public ResponseEntity<?> listarConversas(
            HttpServletRequest request) {

        Usuario usuario = buscarUsuarioLogado(request);

        if (usuario == null) {
            return naoAutenticado();
        }

        List<ConversaResumoDTO> conversas =
                conversaService.buscarPorUsuario(usuario.getId())
                        .stream()
                        .map(conversa -> resumirConversa(
                                conversa,
                                usuario
                        ))
                        .toList();

        return ResponseEntity.ok(conversas);
    }

    @PostMapping("/iniciar/{produtoId}")
    public ResponseEntity<?> iniciarConversa(
            @PathVariable Long produtoId,
            HttpServletRequest request) {

        Usuario usuario = buscarUsuarioLogado(request);

        if (usuario == null) {
            return naoAutenticado();
        }

        Produto produto = produtoService.buscarPorId(produtoId);

        if (produto == null) {
            return ResponseEntity
                    .status(404)
                    .body(Map.of(
                            "erro",
                            "Produto não encontrado."
                    ));
        }

        if (produto.getUsuario() == null
                || produto.getUsuario().getId()
                .equals(usuario.getId())) {

            return ResponseEntity
                    .status(403)
                    .body(Map.of(
                            "erro",
                            "Você não pode iniciar uma conversa sobre seu próprio anúncio."
                    ));
        }

        Conversa conversa = conversaService.buscarOuCriar(
                produto,
                usuario
        );

        return ResponseEntity.ok(Map.of(
                "conversaId",
                conversa.getId()
        ));
    }

    @GetMapping("/{conversaId}")
    public ResponseEntity<?> abrirConversa(
            @PathVariable Long conversaId,
            HttpServletRequest request) {

        Usuario usuario = buscarUsuarioLogado(request);

        if (usuario == null) {
            return naoAutenticado();
        }

        Conversa conversa = conversaService.buscarPorId(conversaId);

        if (conversa == null) {
            return ResponseEntity
                    .status(404)
                    .body(Map.of(
                            "erro",
                            "Conversa não encontrada."
                    ));
        }

        if (!participaDaConversa(conversa, usuario)) {
            return ResponseEntity
                    .status(403)
                    .body(Map.of(
                            "erro",
                            "Você não possui acesso a esta conversa."
                    ));
        }

        mensagemService.marcarComoLidas(
                conversaId,
                usuario.getId()
        );

        List<MensagemDTO> mensagens =
                mensagemService.buscarPorConversa(conversaId)
                        .stream()
                        .map(MensagemDTO::de)
                        .toList();

        return ResponseEntity.ok(Map.of(
                "conversa",
                resumirConversa(conversa, usuario),
                "mensagens",
                mensagens
        ));
    }

    @PostMapping("/{conversaId}/mensagens")
    public ResponseEntity<?> enviarMensagem(
            @PathVariable Long conversaId,
            @RequestBody MensagemRequestDTO requestBody,
            HttpServletRequest request) {

        Usuario usuario = buscarUsuarioLogado(request);

        if (usuario == null) {
            return naoAutenticado();
        }

        Conversa conversa = conversaService.buscarPorId(conversaId);

        if (conversa == null
                || !participaDaConversa(conversa, usuario)) {

            return ResponseEntity
                    .status(403)
                    .body(Map.of(
                            "erro",
                            "Você não possui acesso a esta conversa."
                    ));
        }

        String texto = requestBody.texto();

        if (texto == null
                || texto.trim().isEmpty()
                || texto.trim().length() > 2000) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "erro",
                            "A mensagem deve possuir entre 1 e 2000 caracteres."
                    ));
        }

        Mensagem mensagem = new Mensagem();
        mensagem.setConversa(conversa);
        mensagem.setRemetente(usuario);
        mensagem.setTexto(texto.trim());

        Mensagem mensagemSalva = mensagemService.salvar(mensagem);

        tempoRealService.enviarParaConversa(
                conversaId,
                "mensagem",
                Map.of(
                        "id",
                        mensagemSalva.getId(),
                        "texto",
                        mensagemSalva.getTexto(),
                        "remetenteId",
                        usuario.getId(),
                        "remetenteNome",
                        usuario.getNome(),
                        "dataHora",
                        mensagemSalva.getDataHoraFormatada()
                )
        );

        Usuario destinatario = conversa.getComprador()
                .getId()
                .equals(usuario.getId())
                ? conversa.getVendedor()
                : conversa.getComprador();

        if (destinatario != null) {
            notificacaoService.criar(
                    destinatario,
                    "MENSAGEM",
                    "Você recebeu uma nova mensagem de "
                            + usuario.getNome()
                            + ".",
                    "/chat/" + conversaId
            );
        }

        return ResponseEntity.ok(
                MensagemDTO.de(mensagemSalva)
        );
    }

    private ConversaResumoDTO resumirConversa(
            Conversa conversa,
            Usuario usuario) {

        Usuario outraPessoa = conversa.getComprador()
                .getId()
                .equals(usuario.getId())
                ? conversa.getVendedor()
                : conversa.getComprador();

        Mensagem ultimaMensagem =
                mensagemService.buscarUltimaMensagem(
                        conversa.getId()
                );

        long mensagensNaoLidas =
                mensagemService.contarNaoLidas(
                        conversa.getId(),
                        usuario.getId()
                );

        return new ConversaResumoDTO(
                conversa.getId(),
                conversa.getProduto().getId(),
                conversa.getProduto().getNome(),
                outraPessoa.getId(),
                outraPessoa.getNome(),
                ultimaMensagem != null
                        ? ultimaMensagem.getTexto()
                        : null,
                ultimaMensagem != null
                        ? ultimaMensagem.getDataHora()
                        : null,
                mensagensNaoLidas
        );
    }

    private boolean participaDaConversa(
            Conversa conversa,
            Usuario usuario) {

        Long usuarioId = usuario.getId();

        return (
                conversa.getComprador() != null
                        && conversa.getComprador()
                        .getId()
                        .equals(usuarioId)
        ) || (
                conversa.getVendedor() != null
                        && conversa.getVendedor()
                        .getId()
                        .equals(usuarioId)
        );
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
        return ResponseEntity
                .status(401)
                .body(Map.of(
                        "erro",
                        "Usuário não autenticado."
                ));
    }
}