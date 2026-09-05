package com.leilao.leilao_games.dto;

public record PerfilRequestDTO(
        String nome,
        String email,
        String senhaAtual,
        String novaSenha,
        String confirmarSenha
) {
}