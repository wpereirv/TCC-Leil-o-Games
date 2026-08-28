package com.leilao.leilao_games.dto;

import com.leilao.leilao_games.model.Avaliacao;

public record AvaliacaoDTO(
        Long id,
        Integer nota,
        String comentario,
        String compradorNome
) {

    public static AvaliacaoDTO de(
            Avaliacao avaliacao) {

        return new AvaliacaoDTO(
                avaliacao.getId(),
                avaliacao.getNota(),
                avaliacao.getComentario(),
                avaliacao.getComprador().getNome()
        );
    }
}