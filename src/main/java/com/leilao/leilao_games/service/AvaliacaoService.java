package com.leilao.leilao_games.service;

import com.leilao.leilao_games.model.Avaliacao;
import com.leilao.leilao_games.model.Produto;
import com.leilao.leilao_games.model.Usuario;
import com.leilao.leilao_games.repository.AvaliacaoRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final UsuarioService usuarioService;
    private final ProdutoService produtoService;

    public void salvar(Avaliacao avaliacao) {

        if (avaliacao.getNota() == null
                || avaliacao.getNota() < 1
                || avaliacao.getNota() > 5) {

            throw new IllegalArgumentException(
                    "A nota deve estar entre 1 e 5."
            );
        }

        if (avaliacao.getComentario() != null) {

            avaliacao.setComentario(
                    avaliacao.getComentario().trim()
            );
        }

        avaliacaoRepository.save(avaliacao);

        if (avaliacao.getVendedor() != null) {

            atualizarEstatisticas(
                    avaliacao.getVendedor().getId()
            );
        }
    }

    public List<Avaliacao> listarTodas() {

        return avaliacaoRepository
                .findAllByOrderByIdDesc();
    }

    public Avaliacao buscarPorId(Long id) {

        return avaliacaoRepository
                .findById(id)
                .orElse(null);
    }

    public List<Avaliacao> buscarPorVendedor(
            Long vendedorId) {

        return avaliacaoRepository
                .findByVendedorId(vendedorId);
    }

    public boolean produtoJaAvaliado(
            Long produtoId) {

        return avaliacaoRepository
                .existsByProdutoId(produtoId);
    }

    public long contarAvaliacoes() {

        return avaliacaoRepository.count();
    }

    public void excluir(Long id) {

        Avaliacao avaliacao =
                buscarPorId(id);

        if (avaliacao == null) {

            throw new IllegalArgumentException(
                    "Avaliação não encontrada."
            );
        }

        Long vendedorId =
                avaliacao.getVendedor() != null
                        ? avaliacao.getVendedor().getId()
                        : null;

        Produto produto =
                avaliacao.getProduto();

        avaliacaoRepository.delete(avaliacao);

        if (produto != null) {

            produto.setAvaliado(false);

            produtoService.salvar(produto);
        }

        if (vendedorId != null) {

            atualizarEstatisticas(vendedorId);
        }
    }

    public void removerPorProduto(
            Long produtoId) {

        List<Avaliacao> avaliacoes =
                avaliacaoRepository
                        .findByProdutoId(produtoId);

        Set<Long> vendedores =
                new HashSet<>();

        for (Avaliacao avaliacao : avaliacoes) {

            if (avaliacao.getVendedor() != null) {

                vendedores.add(
                        avaliacao.getVendedor().getId()
                );
            }
        }

        avaliacaoRepository
                .deleteByProdutoId(produtoId);

        for (Long vendedorId : vendedores) {

            atualizarEstatisticas(vendedorId);
        }
    }

    private void atualizarEstatisticas(
            Long vendedorId) {

        Usuario vendedor =
                usuarioService.buscarPorId(vendedorId);

        if (vendedor == null) {
            return;
        }

        List<Avaliacao> avaliacoes =
                avaliacaoRepository
                        .findByVendedorId(vendedorId);

        double media =
                avaliacoes.stream()
                        .filter(avaliacao ->
                                avaliacao.getNota() != null)
                        .mapToInt(avaliacao ->
                                java.util.Objects
                        .requireNonNull(avaliacao.getNota())
                        .intValue()
                        )
                        .average()
                        .orElse(0.0);

        vendedor.setQuantidadeAvaliacoes(
                avaliacoes.size()
        );

        vendedor.setMediaAvaliacoes(media);

        usuarioService.salvarUsuario(vendedor);
    }
}