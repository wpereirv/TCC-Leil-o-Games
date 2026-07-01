package com.leilao.leilao_games.config;

import com.leilao.leilao_games.service.ProdutoService;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class AgendadorLeiloes {

    private final ProdutoService produtoService;

    @Scheduled(
            fixedDelay = 60000,
            initialDelay = 10000
    )
    public void verificarLeiloesEncerrados() {

        produtoService.verificarLeiloesEncerrados();
    }
}