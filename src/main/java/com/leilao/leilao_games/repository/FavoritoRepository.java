package com.leilao.leilao_games.repository;

import com.leilao.leilao_games.model.Favorito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface FavoritoRepository
        extends JpaRepository<Favorito, Long> {

    List<Favorito> findByUsuarioId(Long usuarioId);

    boolean existsByUsuarioIdAndProdutoId(
            Long usuarioId,
            Long produtoId);

    void deleteByUsuarioIdAndProdutoId(
            Long usuarioId,
            Long produtoId);

    long countByUsuarioId(Long usuarioId);

    @Modifying
    @Transactional
    void deleteByProdutoId(Long produtoId);

}