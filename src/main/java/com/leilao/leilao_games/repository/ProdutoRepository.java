package com.leilao.leilao_games.repository;

import com.leilao.leilao_games.model.Produto;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProdutoRepository
        extends JpaRepository<Produto, Long> {

    List<Produto> findByCategoriaId(Long categoriaId);

    List<Produto> findByNomeContainingIgnoreCase(
            String nome
    );

    List<Produto> findByUsuarioId(Long usuarioId);

    List<Produto> findByCompradorId(Long compradorId);

    long countByUsuarioId(Long usuarioId);

    List<Produto> findByEncerradoFalse();

    List<Produto> findByEncerradoTrue();

    List<Produto>
    findByEncerradoFalseOrderByIdDesc();

    List<Produto>
    findByEncerradoFalseOrderByValorInicialAsc();

    List<Produto>
    findByEncerradoFalseOrderByValorInicialDesc();

    List<Produto>
    findByEncerradoFalseOrderByDataFimAsc();

    List<Produto>
    findByEncerradoFalseAndDataFimBefore(
            LocalDateTime dataHora
    );

    long countByEncerradoFalse();

    long countByEncerradoTrue();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select produto
            from Produto produto
            where produto.id = :id
            """)
    Optional<Produto> buscarPorIdComBloqueio(
            @Param("id") Long id
    );
}