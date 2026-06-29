package com.leilao.leilao_games.repository;

import com.leilao.leilao_games.model.Avaliacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AvaliacaoRepository
        extends JpaRepository<Avaliacao, Long> {

    List<Avaliacao> findByVendedorId(Long vendedorId);

    boolean existsByProdutoId(Long produtoId);

    @Modifying
    @Transactional
    void deleteByProdutoId(Long produtoId);

    List<Avaliacao> findAllByOrderByIdDesc();

    List<Avaliacao> findByProdutoId(Long produtoId);

}