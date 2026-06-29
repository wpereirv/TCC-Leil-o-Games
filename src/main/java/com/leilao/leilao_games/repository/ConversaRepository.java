package com.leilao.leilao_games.repository;

import com.leilao.leilao_games.model.Conversa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConversaRepository extends JpaRepository<Conversa, Long> {

    Optional<Conversa> findByProdutoIdAndCompradorId(
            Long produtoId,
            Long compradorId
    );

    List<Conversa> findByCompradorId(Long compradorId);

    List<Conversa> findByVendedorId(Long vendedorId);
}