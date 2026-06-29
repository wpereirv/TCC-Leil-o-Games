package com.leilao.leilao_games.controller;

import com.leilao.leilao_games.model.Notificacao;
import com.leilao.leilao_games.model.Usuario;
import com.leilao.leilao_games.service.NotificacaoService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
public class NotificacoesGlobaisController {

    @Autowired
    private NotificacaoService notificacaoService;

    @ModelAttribute
    public void adicionarDadosGlobais(
            HttpSession session,
            Model model) {

        Usuario usuarioLogado =
                (Usuario) session.getAttribute(
                        "usuarioLogado"
                );

        // Disponibiliza o mesmo usuário em todas as páginas.
        model.addAttribute(
                "usuarioLogado",
                usuarioLogado
        );

        if (usuarioLogado == null) {
            return;
        }

        List<Notificacao> notificacoes =
                notificacaoService.listarPorUsuario(
                        usuarioLogado.getId()
                );

        model.addAttribute(
                "notificacoesCabecalho",
                notificacoes.stream()
                        .limit(5)
                        .toList()
        );

        model.addAttribute(
                "quantidadeNotificacoesNaoLidas",
                notificacaoService.contarNaoLidas(
                        usuarioLogado.getId()
                )
        );
    }
}