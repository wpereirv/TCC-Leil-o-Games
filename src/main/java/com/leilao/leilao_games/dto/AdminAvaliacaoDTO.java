package com.leilao.leilao_games.dto;

import com.leilao.leilao_games.model.Avaliacao;

public record AdminAvaliacaoDTO(
        Long id,
        Integer nota,
        String comentario,
        String compradorNome,
        String vendedorNome,
        String produtoNome
) {

    public static AdminAvaliacaoDTO de(
            Avaliacao avaliacao) {

        return new AdminAvaliacaoDTO(
                avaliacao.getId(),
                avaliacao.getNota(),
                avaliacao.getComentario(),
                avaliacao.getComprador() != null
                        ? avaliacao.getComprador().getNome()
                        : "Usuário removido",
                avaliacao.getVendedor() != null
                        ? avaliacao.getVendedor().getNome()
                        : "Usuário removido",
                avaliacao.getProduto() != null
                        ? avaliacao.getProduto().getNome()
                        : "Produto removido"
        );
    }
}