package com.leilao.leilao_games.dto;

import com.leilao.leilao_games.model.Usuario;

public record UsuarioDTO(
        Long id,
        String nome,
        String email,
        String tipo,
        Double mediaAvaliacoes,
        Integer quantidadeAvaliacoes
) {

    public static UsuarioDTO de(Usuario usuario) {

        return new UsuarioDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getTipo(),
                usuario.getMediaAvaliacoes(),
                usuario.getQuantidadeAvaliacoes()
        );
    }
}