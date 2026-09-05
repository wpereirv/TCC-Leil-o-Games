package com.leilao.leilao_games.dto;

import com.leilao.leilao_games.model.Notificacao;

import java.time.LocalDateTime;

public record NotificacaoDTO(
        Long id,
        String mensagem,
        String tipo,
        String link,
        LocalDateTime dataHora,
        Boolean lida
) {

    public static NotificacaoDTO de(
            Notificacao notificacao) {

        return new NotificacaoDTO(
                notificacao.getId(),
                notificacao.getMensagem(),
                notificacao.getTipo(),
                notificacao.getLink(),
                notificacao.getDataHora(),
                notificacao.getLida()
        );
    }
}