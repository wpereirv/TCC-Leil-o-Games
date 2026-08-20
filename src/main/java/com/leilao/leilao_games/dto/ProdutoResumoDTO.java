package com.leilao.leilao_games.dto;

import com.leilao.leilao_games.model.Categoria;
import com.leilao.leilao_games.model.Produto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProdutoResumoDTO(
        Long id,
        String nome,
        Long categoriaId,
        String categoriaNome,
        BigDecimal valorInicial,
        String imagemPrincipal,
        LocalDateTime dataFim,
        Boolean encerrado
) {

    public static ProdutoResumoDTO de(
            Produto produto) {

        Categoria categoria =
                produto.getCategoria();

        return new ProdutoResumoDTO(
                produto.getId(),
                produto.getNome(),
                categoria != null
                        ? categoria.getId()
                        : null,
                categoria != null
                        ? categoria.getNome()
                        : null,
                produto.getValorInicial(),
                produto.getImagem1(),
                produto.getDataFim(),
                produto.getEncerrado()
        );
    }
}