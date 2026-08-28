package com.leilao.leilao_games.dto;

import java.time.LocalDateTime;

public record ConversaResumoDTO(
        Long id,
        Long produtoId,
        String produtoNome,
        Long outraPessoaId,
        String outraPessoaNome,
        String ultimaMensagem,
        LocalDateTime dataHoraUltimaMensagem,
        long mensagensNaoLidas
) {
}