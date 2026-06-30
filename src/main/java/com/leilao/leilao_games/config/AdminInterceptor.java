package com.leilao.leilao_games.config;

import com.leilao.leilao_games.model.Usuario;
import com.leilao.leilao_games.service.UsuarioService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminInterceptor
        implements HandlerInterceptor {

    private final UsuarioService usuarioService;

    public AdminInterceptor(
            UsuarioService usuarioService) {

        this.usuarioService = usuarioService;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        HttpSession session =
                request.getSession(false);

        if (session == null) {

            response.sendRedirect("/login");

            return false;
        }

        Usuario usuarioSessao =
                (Usuario) session.getAttribute(
                        "usuarioLogado"
                );

        if (usuarioSessao == null
                || usuarioSessao.getId() == null) {

            session.invalidate();

            response.sendRedirect("/login");

            return false;
        }

        Usuario usuarioAtual =
                usuarioService.buscarPorId(
                        usuarioSessao.getId()
                );

        if (usuarioAtual == null) {

            session.invalidate();

            response.sendRedirect("/login");

            return false;
        }

        if (!"ADMIN".equalsIgnoreCase(
                usuarioAtual.getTipo())) {

            session.setAttribute(
                    "usuarioLogado",
                    usuarioAtual
            );

            response.sendRedirect("/");

            return false;
        }

        session.setAttribute(
                "usuarioLogado",
                usuarioAtual
        );

        return true;
    }
}