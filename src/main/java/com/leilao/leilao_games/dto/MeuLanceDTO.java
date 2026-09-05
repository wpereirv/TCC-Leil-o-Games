package com.leilao.leilao_games.dto;

import com.leilao.leilao_games.model.Lance;
import com.leilao.leilao_games.model.Produto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MeuLanceDTO(
        Long id,
        Long produtoId,
        String produtoNome,
        String imagemPrincipal,
        BigDecimal valor,
        LocalDateTime dataHora,
        Boolean encerrado
) {

    public static MeuLanceDTO de(Lance lance) {

        Produto produto = lance.getProduto();

        return new MeuLanceDTO(
                lance.getId(),
                produto != null ? produto.getId() : null,
                produto != null ? produto.getNome() : null,
                produto != null ? produto.getImagem1() : null,
                lance.getValor(),
                lance.getDataHora(),
                produto != null ? produto.getEncerrado() : null
        );
    }
}