package com.leilao.leilao_games.service;

import com.leilao.leilao_games.model.Mensagem;
import com.leilao.leilao_games.repository.MensagemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MensagemService {

    private final MensagemRepository mensagemRepository;

    public Mensagem salvar(Mensagem mensagem) {

        if (mensagem.getDataHora() == null) {
            mensagem.setDataHora(LocalDateTime.now());
        }

        if (mensagem.getLida() == null) {
            mensagem.setLida(false);
        }

        return mensagemRepository.save(mensagem);
    }

    public List<Mensagem> buscarPorConversa(Long conversaId) {

        return mensagemRepository
                .findByConversaIdOrderByDataHoraAsc(conversaId);
    }

    public void marcarComoLidas(
            Long conversaId,
            Long usuarioId) {

        List<Mensagem> mensagens =
                mensagemRepository
                        .findByConversaIdAndRemetenteIdNotAndLidaFalse(
                                conversaId,
                                usuarioId
                        );

        for (Mensagem mensagem : mensagens) {
            mensagem.setLida(true);
        }

        mensagemRepository.saveAll(mensagens);
    }

    public long contarNaoLidas(
        Long conversaId,
        Long usuarioId) {

    return mensagemRepository
            .countByConversaIdAndRemetenteIdNotAndLidaFalse(
                    conversaId,
                    usuarioId
            );
    }

    public Mensagem buscarUltimaMensagem(Long conversaId) {

    return mensagemRepository
            .findFirstByConversaIdOrderByDataHoraDesc(
                    conversaId
            )
            .orElse(null);
    }
}