package com.leilao.leilao_games.controller;

import com.leilao.leilao_games.dto.NotificacaoDTO;
import com.leilao.leilao_games.model.Notificacao;
import com.leilao.leilao_games.model.Usuario;
import com.leilao.leilao_games.service.NotificacaoService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@CrossOrigin(
        origins = "http://localhost:5173",
        allowCredentials = "true"
)
@RestController
@RequestMapping("/api/notificacoes")
@RequiredArgsConstructor
public class ApiNotificacaoController {

    private final NotificacaoService notificacaoService;

    @GetMapping
    public ResponseEntity<?> listar(
            HttpServletRequest request) {

        Usuario usuario = buscarUsuarioLogado(request);

        if (usuario == null) {
            return naoAutenticado();
        }

        List<NotificacaoDTO> notificacoes =
                notificacaoService
                        .listarPorUsuario(usuario.getId())
                        .stream()
                        .map(NotificacaoDTO::de)
                        .toList();

        return ResponseEntity.ok(notificacoes);
    }

    @GetMapping("/quantidade")
    public ResponseEntity<?> quantidadeNaoLidas(
            HttpServletRequest request) {

        Usuario usuario = buscarUsuarioLogado(request);

        if (usuario == null) {
            return naoAutenticado();
        }

        return ResponseEntity.ok(Map.of(
                "quantidade",
                notificacaoService.contarNaoLidas(
                        usuario.getId()
                )
        ));
    }

    @PostMapping("/{notificacaoId}/abrir")
    public ResponseEntity<?> abrir(
            @PathVariable Long notificacaoId,
            HttpServletRequest request) {

        Usuario usuario = buscarUsuarioLogado(request);

        if (usuario == null) {
            return naoAutenticado();
        }

        Notificacao notificacao =
                notificacaoService.marcarComoLida(
                        notificacaoId,
                        usuario.getId()
                );

        if (notificacao == null) {
            return ResponseEntity.notFound().build();
        }

        String link = notificacao.getLink();

        if (link == null
                || !link.startsWith("/")
                || link.startsWith("//")) {

            link = "/notificacoes";
        }

        return ResponseEntity.ok(Map.of(
                "link",
                link
        ));
    }

    @PostMapping("/marcar-todas")
    public ResponseEntity<?> marcarTodas(
            HttpServletRequest request) {

        Usuario usuario = buscarUsuarioLogado(request);

        if (usuario == null) {
            return naoAutenticado();
        }

        notificacaoService.marcarTodasComoLidas(
                usuario.getId()
        );

        return ResponseEntity.ok(Map.of(
                "mensagem",
                "Notificações marcadas como lidas."
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