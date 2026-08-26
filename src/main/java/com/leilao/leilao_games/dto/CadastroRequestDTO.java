package com.leilao.leilao_games.dto;

public record CadastroRequestDTO(
        String nome,
        String email,
        String senha
) {
}