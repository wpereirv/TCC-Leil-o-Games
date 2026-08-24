package com.leilao.leilao_games.dto;

import com.leilao.leilao_games.model.Lance;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LanceDTO(
        Long id,
        Long usuarioId,
        String usuarioNome,
        BigDecimal valor,
        LocalDateTime dataHora
) {

    public static LanceDTO de(Lance lance) {

        return new LanceDTO(
                lance.getId(),
                lance.getUsuario().getId(),
                lance.getUsuario().getNome(),
                lance.getValor(),
                lance.getDataHora()
        );
    }
}