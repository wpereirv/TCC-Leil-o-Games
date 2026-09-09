package com.leilao.leilao_games.dto;

import com.leilao.leilao_games.model.Produto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminProdutoDTO(
        Long id,
        String nome,
        String categoriaNome,
        String vendedorNome,
        BigDecimal valorInicial,
        LocalDateTime dataInicio,
        LocalDateTime dataFim,
        Boolean encerrado,
        Boolean ativo,
        Integer quantidadeLances
) {

    public static AdminProdutoDTO de(
            Produto produto,
            Integer quantidadeLances) {

        return new AdminProdutoDTO(
                produto.getId(),
                produto.getNome(),
                produto.getCategoria() != null
                        ? produto.getCategoria().getNome()
                        : "Sem categoria",
                produto.getUsuario() != null
                        ? produto.getUsuario().getNome()
                        : "Usuário removido",
                produto.getValorInicial(),
                produto.getDataInicio(),
                produto.getDataFim(),
                produto.getEncerrado(),
                produto.getAtivo(),
                quantidadeLances
        );
    }
}