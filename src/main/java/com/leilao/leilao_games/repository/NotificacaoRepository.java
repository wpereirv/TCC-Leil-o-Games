package com.leilao.leilao_games.repository;

import com.leilao.leilao_games.model.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificacaoRepository
        extends JpaRepository<Notificacao, Long> {

    List<Notificacao> findByUsuarioIdOrderByDataHoraDesc(
            Long usuarioId
    );

    List<Notificacao> findByUsuarioIdAndLidaFalse(
            Long usuarioId
    );

    long countByUsuarioIdAndLidaFalse(
            Long usuarioId
    );

    Optional<Notificacao> findByIdAndUsuarioId(
            Long id,
            Long usuarioId
    );
}