package com.leilao.leilao_games.dto;

import com.leilao.leilao_games.model.Categoria;
import com.leilao.leilao_games.model.Produto;
import com.leilao.leilao_games.model.Usuario;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProdutoDetalheDTO(
        Long id,
        String nome,
        String descricao,
        Long categoriaId,
        String categoriaNome,
        BigDecimal valorInicial,
        BigDecimal maiorLance,
        String imagem1,
        String imagem2,
        String imagem3,
        LocalDateTime dataInicio,
        LocalDateTime dataFim,
        Boolean encerrado,
        Long vendedorId,
        String vendedorNome
) {

    public static ProdutoDetalheDTO de(
            Produto produto,
            BigDecimal maiorLance) {

        Categoria categoria =
                produto.getCategoria();

        Usuario vendedor =
                produto.getUsuario();

        return new ProdutoDetalheDTO(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                categoria != null
                        ? categoria.getId()
                        : null,
                categoria != null
                        ? categoria.getNome()
                        : null,
                produto.getValorInicial(),
                maiorLance,
                produto.getImagem1(),
                produto.getImagem2(),
                produto.getImagem3(),
                produto.getDataInicio(),
                produto.getDataFim(),
                produto.getEncerrado(),
                vendedor != null
                        ? vendedor.getId()
                        : null,
                vendedor != null
                        ? vendedor.getNome()
                        : null
        );
    }
}