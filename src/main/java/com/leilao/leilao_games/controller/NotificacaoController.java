package com.leilao.leilao_games.controller;

import com.leilao.leilao_games.model.Notificacao;
import com.leilao.leilao_games.model.Usuario;
import com.leilao.leilao_games.service.NotificacaoService;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import com.leilao.leilao_games.dto.NotificacaoResumoDTO;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class NotificacaoController {

    private final NotificacaoService notificacaoService;

    @GetMapping("/notificacoes")
    public String listar(
            HttpSession session,
            Model model) {

        Usuario usuarioLogado =
                (Usuario) session.getAttribute("usuarioLogado");

        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        model.addAttribute(
                "notificacoes",
                notificacaoService.listarPorUsuario(
                        usuarioLogado.getId()
                )
        );

        model.addAttribute(
                "quantidadeNaoLidas",
                notificacaoService.contarNaoLidas(
                        usuarioLogado.getId()
                )
        );

        model.addAttribute(
                "usuarioLogado",
                usuarioLogado
        );

        return "notificacoes";
    }

    @GetMapping("/notificacoes/abrir/{id}")
    public String abrir(
            @PathVariable Long id,
            HttpSession session) {

        Usuario usuarioLogado =
                (Usuario) session.getAttribute("usuarioLogado");

        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        Notificacao notificacao =
                notificacaoService.marcarComoLida(
                        id,
                        usuarioLogado.getId()
                );

        if (notificacao == null) {
            return "redirect:/notificacoes";
        }

        String link = notificacao.getLink();

        if (link == null
                || !link.startsWith("/")
                || link.startsWith("//")) {

            return "redirect:/notificacoes";
        }

        return "redirect:" + link;
    }

    @PostMapping("/notificacoes/marcar-todas")
    public String marcarTodas(HttpSession session) {

        Usuario usuarioLogado =
                (Usuario) session.getAttribute("usuarioLogado");

        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        notificacaoService.marcarTodasComoLidas(
                usuarioLogado.getId()
        );

        return "redirect:/notificacoes";
    }

    @GetMapping("/notificacoes/quantidade")
@ResponseBody
public long quantidadeNaoLidas(
        HttpSession session) {

    Usuario usuarioLogado =
            (Usuario) session.getAttribute(
                    "usuarioLogado"
            );

    if (usuarioLogado == null) {
        return 0;
    }

    return notificacaoService.contarNaoLidas(
            usuarioLogado.getId()
    );
        }

        @GetMapping("/notificacoes/recentes")
@ResponseBody
public List<NotificacaoResumoDTO> notificacoesRecentes(
        HttpSession session) {

    Usuario usuarioLogado =
            (Usuario) session.getAttribute(
                    "usuarioLogado"
            );

    if (usuarioLogado == null) {
        return List.of();
    }

    return notificacaoService
            .listarPorUsuario(usuarioLogado.getId())
            .stream()
            .limit(5)
            .map(notificacao ->
                    new NotificacaoResumoDTO(
                            notificacao.getId(),
                            notificacao.getMensagem(),
                            notificacao.getTipo(),
                            notificacao.getDataHoraFormatada(),
                            notificacao.getLida()
                    )
            )
            .toList();
}
}