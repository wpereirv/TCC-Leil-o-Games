package com.leilao.leilao_games.repository;

import com.leilao.leilao_games.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository
        extends JpaRepository<Categoria, Long> {

    boolean existsByNomeIgnoreCase(
            String nome
    );

    boolean existsByNomeIgnoreCaseAndIdNot(
            String nome,
            Long id
    );
}