package com.leilao.leilao_games.dto;

import com.leilao.leilao_games.model.Categoria;

public record CategoriaDTO(
        Long id,
        String nome,
        String descricao
) {

    public static CategoriaDTO de(
            Categoria categoria) {

        return new CategoriaDTO(
                categoria.getId(),
                categoria.getNome(),
                categoria.getDescricao()
        );
    }
}