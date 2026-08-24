package com.leilao.leilao_games.dto;

import java.math.BigDecimal;

public record RegistrarLanceRequestDTO(
        BigDecimal valor
) {
}