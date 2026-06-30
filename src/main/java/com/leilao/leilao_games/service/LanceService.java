package com.leilao.leilao_games.service;

import com.leilao.leilao_games.model.Lance;
import com.leilao.leilao_games.model.Produto;
import com.leilao.leilao_games.model.Usuario;
import com.leilao.leilao_games.repository.LanceRepository;
import com.leilao.leilao_games.repository.ProdutoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
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
            Produto produto
    ) {
    }

    @Autowired
    private LanceRepository lanceRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Transactional
    public Registro registrar(
            Long produtoId,
            Double valor,
            Usuario usuario) {

        if (produtoId == null
                || valor == null
                || !Double.isFinite(valor)
                || valor <= 0
                || usuario == null) {

            return new Registro(
                    Resultado.VALOR_INVALIDO,
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
                    null
            );
        }

        if (Boolean.TRUE.equals(
                produto.getEncerrado()
        )) {

            return new Registro(
                    Resultado.ENCERRADO,
                    produto
            );
        }

        if (produto.getDataFim() != null
                && !LocalDateTime.now().isBefore(
                        produto.getDataFim()
                )) {

            return new Registro(
        Resultado.ENCERRADO,
        produto
);
        }

        if (produto.getUsuario() == null
                || produto.getUsuario()
                        .getId()
                        .equals(usuario.getId())) {

            return new Registro(
                    Resultado.VENDEDOR,
                    produto
            );
        }

        Lance maiorLance =
                lanceRepository
                        .findFirstByProdutoIdOrderByValorDesc(
                                produtoId
                        );

        if (maiorLance == null) {

            if (produto.getValorInicial() == null
                    || valor < produto.getValorInicial()) {

                return new Registro(
                        Resultado.VALOR_INICIAL,
                        produto
                );
            }

        } else if (valor <= maiorLance.getValor()) {

            return new Registro(
                    Resultado.LANCE_MENOR,
                    produto
            );
        }

        Lance lance = new Lance();

        lance.setValor(valor);
        lance.setProduto(produto);
        lance.setUsuario(usuario);

        lanceRepository.save(lance);

        return new Registro(
                Resultado.SUCESSO,
                produto
        );
    }

    public void salvar(Lance lance) {
        lanceRepository.save(lance);
    }

    public Double buscarMaiorLance(Long produtoId) {

        Lance lance =
                lanceRepository
                        .findFirstByProdutoIdOrderByValorDesc(
                                produtoId
                        );

        if (lance == null) {
            return 0.0;
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