package com.leilao.leilao_games.repository;

import com.leilao.leilao_games.model.TokenRecuperacaoSenha;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TokenRecuperacaoSenhaRepository
        extends JpaRepository<
                TokenRecuperacaoSenha,
                Long
        > {

    Optional<TokenRecuperacaoSenha>
    findByTokenHashAndUtilizadoFalse(
            String tokenHash
    );

    List<TokenRecuperacaoSenha>
    findByUsuarioIdAndUtilizadoFalse(
            Long usuarioId
    );
}