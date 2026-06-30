package com.leilao.leilao_games.controller;

import com.leilao.leilao_games.model.Conversa;
import com.leilao.leilao_games.model.Usuario;
import com.leilao.leilao_games.service.ConversaService;
import com.leilao.leilao_games.service.TempoRealService;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Controller
public class TempoRealController {

    private final TempoRealService tempoRealService;

    private final ConversaService conversaService;

    public TempoRealController(
            TempoRealService tempoRealService,
            ConversaService conversaService) {

        this.tempoRealService = tempoRealService;
        this.conversaService = conversaService;
    }

    @GetMapping(
            value = "/tempo-real",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    @ResponseBody
    public SseEmitter conectar(
            @RequestParam(
                    required = false
            )
            Long produtoId,

            @RequestParam(
                    required = false
            )
            Long conversaId,

            HttpSession session) {

        Usuario usuario =
                (Usuario) session.getAttribute(
                        "usuarioLogado"
                );

        Long usuarioId =
                usuario != null
                        ? usuario.getId()
                        : null;

        if (conversaId != null) {

            if (usuario == null) {
                throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED
                );
            }

            Conversa conversa =
                    conversaService.buscarPorId(
                            conversaId
                    );

            if (conversa == null
                    || !participa(
                            conversa,
                            usuarioId
                    )) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN
                );
            }
        }

        return tempoRealService.conectar(
                usuarioId,
                produtoId,
                conversaId
        );
    }

    private boolean participa(
            Conversa conversa,
            Long usuarioId) {

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
}