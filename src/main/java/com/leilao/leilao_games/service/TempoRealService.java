package com.leilao.leilao_games.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class TempoRealService {

    private static final long TEMPO_LIMITE =
            30L * 60 * 1000;

    private final Map<
            Long,
            CopyOnWriteArrayList<SseEmitter>
    > usuarios = new ConcurrentHashMap<>();

    private final Map<
            Long,
            CopyOnWriteArrayList<SseEmitter>
    > produtos = new ConcurrentHashMap<>();

    private final Map<
            Long,
            CopyOnWriteArrayList<SseEmitter>
    > conversas = new ConcurrentHashMap<>();

    public SseEmitter conectar(
            Long usuarioId,
            Long produtoId,
            Long conversaId) {

        SseEmitter emitter =
                new SseEmitter(TEMPO_LIMITE);

        if (usuarioId != null) {
            adicionar(usuarios, usuarioId, emitter);
        }

        if (produtoId != null) {
            adicionar(produtos, produtoId, emitter);
        }

        if (conversaId != null) {
            adicionar(conversas, conversaId, emitter);
        }

        Runnable remover = () -> {

            remover(usuarios, usuarioId, emitter);
            remover(produtos, produtoId, emitter);
            remover(conversas, conversaId, emitter);
        };

        emitter.onCompletion(remover);
        emitter.onTimeout(remover);
        emitter.onError(erro -> remover.run());

        try {

            emitter.send(
                    SseEmitter.event()
                            .name("conectado")
                            .data(Map.of(
                                    "ativo",
                                    true
                            ))
            );

        } catch (IOException erro) {

            remover.run();
            emitter.completeWithError(erro);
        }

        return emitter;
    }

    public void enviarParaUsuario(
            Long usuarioId,
            String evento,
            Object dados) {

        enviar(usuarios, usuarioId, evento, dados);
    }

    public void enviarParaProduto(
            Long produtoId,
            String evento,
            Object dados) {

        enviar(produtos, produtoId, evento, dados);
    }

    public void enviarParaConversa(
            Long conversaId,
            String evento,
            Object dados) {

        enviar(conversas, conversaId, evento, dados);
    }

    private void adicionar(
            Map<
                    Long,
                    CopyOnWriteArrayList<SseEmitter>
            > mapa,
            Long chave,
            SseEmitter emitter) {

        mapa.computeIfAbsent(
                chave,
                valor ->
                        new CopyOnWriteArrayList<>()
        ).add(emitter);
    }

    private void remover(
            Map<
                    Long,
                    CopyOnWriteArrayList<SseEmitter>
            > mapa,
            Long chave,
            SseEmitter emitter) {

        if (chave == null) {
            return;
        }

        List<SseEmitter> lista =
                mapa.get(chave);

        if (lista == null) {
            return;
        }

        lista.remove(emitter);

        if (lista.isEmpty()) {
            mapa.remove(chave);
        }
    }

    private void enviar(
            Map<
                    Long,
                    CopyOnWriteArrayList<SseEmitter>
            > mapa,
            Long chave,
            String evento,
            Object dados) {

        if (chave == null) {
            return;
        }

        List<SseEmitter> lista =
                mapa.get(chave);

        if (lista == null) {
            return;
        }

        for (SseEmitter emitter : lista) {

            try {

                emitter.send(
                        SseEmitter.event()
                                .name(evento)
                                .data(dados)
                );

            } catch (IOException erro) {

                emitter.complete();

                remover(
                        mapa,
                        chave,
                        emitter
                );
            }
        }
    }
}