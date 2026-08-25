package com.leilao.leilao_games.dto;

public record ResumoContaDTO(
        UsuarioDTO usuario,
        long totalAnuncios,
        long totalLances,
        long totalFavoritos
) {
}