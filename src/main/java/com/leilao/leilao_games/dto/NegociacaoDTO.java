package com.leilao.leilao_games.dto;

import com.leilao.leilao_games.model.Produto;
import com.leilao.leilao_games.model.StatusNegociacao;
import com.leilao.leilao_games.model.Usuario;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record NegociacaoDTO(
        Long produtoId,
        String produtoNome,
        String imagemPrincipal,
        String vendedorNome,
        String compradorNome,
        BigDecimal valorFinal,
        String status,
        String statusDescricao,
        String codigoRastreio,
        LocalDateTime dataPagamento,
        LocalDateTime dataEnvio,
        LocalDateTime dataConclusao,
        boolean souComprador,
        boolean souVendedor
) {

    public static NegociacaoDTO de(
            Produto produto,
            Long usuarioId) {

        Usuario vendedor = produto.getUsuario();
        Usuario comprador = produto.getComprador();

        StatusNegociacao status =
                produto.getStatusNegociacao();

        boolean souComprador =
                comprador != null
                && comprador.getId().equals(usuarioId);

        boolean souVendedor =
                vendedor != null
                && vendedor.getId().equals(usuarioId);

        return new NegociacaoDTO(
                produto.getId(),
                produto.getNome(),
                produto.getImagem1(),
                vendedor != null ? vendedor.getNome() : null,
                comprador != null ? comprador.getNome() : null,
                produto.getValorFinal(),
                status != null ? status.name() : null,
                status != null
                        ? status.getDescricao()
                        : "Aguardando definição",
                produto.getCodigoRastreio(),
                produto.getDataPagamento(),
                produto.getDataEnvio(),
                produto.getDataConclusao(),
                souComprador,
                souVendedor
        );
    }
}