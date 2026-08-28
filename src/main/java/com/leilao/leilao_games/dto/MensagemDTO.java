package com.leilao.leilao_games.dto;

import com.leilao.leilao_games.model.Mensagem;

import java.time.LocalDateTime;

public record MensagemDTO(
        Long id,
        Long remetenteId,
        String remetenteNome,
        String texto,
        LocalDateTime dataHora
) {

    public static MensagemDTO de(
            Mensagem mensagem) {

        return new MensagemDTO(
                mensagem.getId(),
                mensagem.getRemetente().getId(),
                mensagem.getRemetente().getNome(),
                mensagem.getTexto(),
                mensagem.getDataHora()
        );
    }
}