package com.leilao.leilao_games.service;

import com.leilao.leilao_games.model.Lance;
import com.leilao.leilao_games.model.Produto;
import com.leilao.leilao_games.model.Usuario;
import com.leilao.leilao_games.repository.LanceRepository;
import com.leilao.leilao_games.repository.ProdutoRepository;
import java.math.BigDecimal;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LanceService {

    public enum Resultado {
        SUCESSO,
        PRODUTO_INEXISTENTE,
        ENCERRADO,
        VENDEDOR,
        VALOR_INVALIDO,
        VALOR_INICIAL,
        LANCE_MENOR
    }

    public record Registro(
        Resultado resultado,
        Produto produto,
        Lance lance,
        Usuario usuarioSuperado
        ) {
        }

    private final LanceRepository lanceRepository;
private final ProdutoRepository produtoRepository;

    @Transactional
    public Registro registrar(
            Long produtoId,
            BigDecimal valor,
            Usuario usuario) {

        if (produtoId == null
        || valor == null
        || valor.compareTo(BigDecimal.ZERO) <= 0
        || valor.scale() > 2
        || usuario == null) {

            return new Registro(
                    Resultado.VALOR_INVALIDO,
                    null,
                    null,
                    null
            );
        }

        Produto produto =
                produtoRepository
                        .buscarPorIdComBloqueio(
                                produtoId
                        )
                        .orElse(null);

        if (produto == null) {
            return new Registro(
                    Resultado.PRODUTO_INEXISTENTE,
                    null,
                    null,
                    null
            );
        }

        if (Boolean.TRUE.equals(
                produto.getEncerrado()
        )) {

            return new Registro(
                    Resultado.ENCERRADO,
                    produto,
                    null,
                    null
            );
        }

        if (produto.getDataFim() != null
                && !LocalDateTime.now().isBefore(
                        produto.getDataFim()
                )) {

            return new Registro(
                Resultado.ENCERRADO,
                produto,
                null,
                null
);
        }

        if (produto.getUsuario() == null
                || produto.getUsuario()
                        .getId()
                        .equals(usuario.getId())) {

            return new Registro(
                    Resultado.VENDEDOR,
                    produto,
                    null,
                    null
            );
        }

        Lance maiorLance =
                lanceRepository
                        .findFirstByProdutoIdOrderByValorDesc(
                                produtoId
                        );

        if (maiorLance == null) {

            if (produto.getValorInicial() == null
                || valor.compareTo(produto.getValorInicial()) < 0) {

                return new Registro(
                        Resultado.VALOR_INICIAL,
                        produto,
                        null,
                        null
                );
            }

        } else if (valor.compareTo(maiorLance.getValor()) <= 0) {

            return new Registro(
                    Resultado.LANCE_MENOR,
                    produto,
                    null,
                    null
            );
        }

        Lance lance = new Lance();

        lance.setValor(valor);
        lance.setProduto(produto);
        lance.setUsuario(usuario);

        lanceRepository.save(lance);

        return new Registro(
                Resultado.SUCESSO,
                produto,
                lance,
                maiorLance != null
                        ? maiorLance.getUsuario()
                        : null
        );
    }

    public void salvar(Lance lance) {
        lanceRepository.save(lance);
    }

    public BigDecimal buscarMaiorLance(Long produtoId) {

        Lance lance =
                lanceRepository
                        .findFirstByProdutoIdOrderByValorDesc(
                                produtoId
                        );

        if (lance == null) {
            return BigDecimal.ZERO;
        }

        return lance.getValor();
    }

    public long contarLances() {
        return lanceRepository.count();
    }

    public long contarLancesUsuario(
            Long usuarioId) {

        return lanceRepository
                .countByUsuarioId(usuarioId);
    }

    public List<Lance> buscarPorUsuario(
            Long usuarioId) {

        return lanceRepository
                .findByUsuarioId(usuarioId);
    }

    public List<Lance> buscarPorProduto(
            Long produtoId) {

        return lanceRepository
                .findByProdutoIdOrderByValorDesc(
                        produtoId
                );
    }

    public Lance buscarLanceVencedor(
            Long produtoId) {

        return lanceRepository
                .findFirstByProdutoIdOrderByValorDesc(
                        produtoId
                );
    }

    public void removerPorProduto(
            Long produtoId) {

        lanceRepository.deleteByProdutoId(
                produtoId
        );
    }
}