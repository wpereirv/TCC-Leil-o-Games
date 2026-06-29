package com.leilao.leilao_games.repository;

import com.leilao.leilao_games.model.Mensagem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MensagemRepository
        extends JpaRepository<Mensagem, Long> {

    List<Mensagem> findByConversaIdOrderByDataHoraAsc(
            Long conversaId
    );

    List<Mensagem> findByConversaIdAndRemetenteIdNotAndLidaFalse(
            Long conversaId,
            Long usuarioId
    );

    long countByConversaIdAndRemetenteIdNotAndLidaFalse(
        Long conversaId,
        Long usuarioId
        );
        
    Optional<Mensagem> findFirstByConversaIdOrderByDataHoraDesc(
        Long conversaId
);
}