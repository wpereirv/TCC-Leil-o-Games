package com.leilao.leilao_games.config;

import com.leilao.leilao_games.service.ProdutoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class AgendadorLeiloes {

    @Autowired
    private ProdutoService produtoService;

    @Scheduled(
            fixedDelay = 60000,
            initialDelay = 10000
    )
    public void verificarLeiloesEncerrados() {

        produtoService.verificarLeiloesEncerrados();
    }
}