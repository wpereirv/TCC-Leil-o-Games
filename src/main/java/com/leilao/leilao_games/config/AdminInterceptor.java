package com.leilao.leilao_games.config;

import com.leilao.leilao_games.model.Usuario;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminInterceptor
        implements HandlerInterceptor {

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

        Usuario usuario =
                (Usuario) session.getAttribute(
                        "usuarioLogado"
                );

        if (usuario == null) {

            response.sendRedirect("/login");

            return false;
        }

        if (!"ADMIN".equalsIgnoreCase(
                usuario.getTipo())) {

            response.sendRedirect("/");

            return false;
        }

        return true;
    }
}