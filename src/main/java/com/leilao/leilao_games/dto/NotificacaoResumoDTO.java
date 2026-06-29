package com.leilao.leilao_games.dto;

public record NotificacaoResumoDTO(
        Long id,
        String mensagem,
        String tipo,
        String dataHora,
        Boolean lida
) {
}