package com.leilao.leilao_games.repository;

import com.leilao.leilao_games.model.Lance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface LanceRepository
        extends JpaRepository<Lance, Long> {

    List<Lance> findByProdutoIdOrderByValorDesc(Long produtoId);

    List<Lance> findByUsuarioId(Long usuarioId);

    Lance findFirstByProdutoIdOrderByValorDesc(Long produtoId);

    long countByUsuarioId(Long usuarioId);

    @Modifying
    @Transactional
    void deleteByProdutoId(Long produtoId);

}