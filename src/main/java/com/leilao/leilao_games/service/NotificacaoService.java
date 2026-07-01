package com.leilao.leilao_games.service;

import com.leilao.leilao_games.model.Notificacao;
import com.leilao.leilao_games.model.Usuario;
import com.leilao.leilao_games.repository.NotificacaoRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;
    private final TempoRealService tempoRealService;

    public Notificacao criar(
        Usuario usuario,
        String tipo,
        String mensagem,
        String link) {

    Notificacao notificacao =
            new Notificacao();

    notificacao.setUsuario(usuario);
    notificacao.setTipo(tipo);
    notificacao.setMensagem(mensagem);
    notificacao.setLink(link);
    notificacao.setDataHora(
            LocalDateTime.now()
    );
    notificacao.setLida(false);

    Notificacao salva =
            notificacaoRepository.save(
                    notificacao
            );

    if (usuario != null
            && usuario.getId() != null) {

        long naoLidas =
                contarNaoLidas(
                        usuario.getId()
                );

        tempoRealService.enviarParaUsuario(
                usuario.getId(),
                "notificacao",
                Map.of(
                        "id",
                        salva.getId(),
                        "tipo",
                        tipo != null ? tipo : "",
                        "mensagem",
                        mensagem != null
                                ? mensagem
                                : "",
                        "link",
                        link != null ? link : "",
                        "dataHora",
                        salva.getDataHoraFormatada(),
                        "naoLidas",
                        naoLidas
                )
        );
    }

    return salva;
}

    public List<Notificacao> listarPorUsuario(
            Long usuarioId) {

        return notificacaoRepository
                .findByUsuarioIdOrderByDataHoraDesc(
                        usuarioId
                );
    }

    public long contarNaoLidas(Long usuarioId) {

        return notificacaoRepository
                .countByUsuarioIdAndLidaFalse(
                        usuarioId
                );
    }

    public Notificacao marcarComoLida(
            Long notificacaoId,
            Long usuarioId) {

        Notificacao notificacao =
                notificacaoRepository
                        .findByIdAndUsuarioId(
                                notificacaoId,
                                usuarioId
                        )
                        .orElse(null);

        if (notificacao != null) {
            notificacao.setLida(true);
            notificacaoRepository.save(notificacao);
        }

        return notificacao;
    }

    public void marcarTodasComoLidas(Long usuarioId) {

        List<Notificacao> notificacoes =
                notificacaoRepository
                        .findByUsuarioIdAndLidaFalse(
                                usuarioId
                        );

        for (Notificacao notificacao : notificacoes) {
            notificacao.setLida(true);
        }

        notificacaoRepository.saveAll(notificacoes);
    }
}