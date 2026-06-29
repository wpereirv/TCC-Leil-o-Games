package com.leilao.leilao_games.repository;

import com.leilao.leilao_games.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.time.LocalDateTime;

public interface ProdutoRepository
        extends JpaRepository<Produto, Long> {

    List<Produto> findByCategoriaId(Long categoriaId);

    List<Produto> findByNomeContainingIgnoreCase(String nome);

    List<Produto> findByUsuarioId(Long usuarioId);

    List<Produto> findByCompradorId(Long compradorId);

    long countByUsuarioId(Long usuarioId);

    List<Produto> findByEncerradoFalse();

    List<Produto> findByEncerradoTrue();

    List<Produto> findByEncerradoFalseOrderByIdDesc();

    List<Produto> findByEncerradoFalseOrderByValorInicialAsc();

    List<Produto> findByEncerradoFalseOrderByValorInicialDesc();

    List<Produto> findByEncerradoFalseOrderByDataFimAsc();

    List<Produto> findByEncerradoFalseAndDataFimBefore(
        LocalDateTime dataHora
    );

    long countByEncerradoFalse();

    long countByEncerradoTrue();

}