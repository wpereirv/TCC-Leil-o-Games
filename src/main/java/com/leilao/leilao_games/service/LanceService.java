package com.leilao.leilao_games.service;

import com.leilao.leilao_games.model.Lance;
import com.leilao.leilao_games.repository.LanceRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LanceService {

    @Autowired
    private LanceRepository lanceRepository;

    public void salvar(Lance lance) {
        lanceRepository.save(lance);
    }

    public Double buscarMaiorLance(Long produtoId) {

        List<Lance> lances =
                lanceRepository.findByProdutoIdOrderByValorDesc(produtoId);

        if (lances.isEmpty()) {
            return 0.0;
        }

        return lances.get(0).getValor();
    }

    public long contarLances() {
        return lanceRepository.count();
    }

    public long contarLancesUsuario(Long usuarioId) {

        return lanceRepository
                .findByUsuarioId(usuarioId)
                .size();

    }

    public List<Lance> buscarPorUsuario(Long usuarioId) {
        return lanceRepository.findByUsuarioId(usuarioId);
    }

    public List<Lance> buscarPorProduto(Long produtoId) {

        return lanceRepository
                .findByProdutoIdOrderByValorDesc(produtoId);

    }

    public Lance buscarLanceVencedor(Long produtoId) {

        return lanceRepository
                .findFirstByProdutoIdOrderByValorDesc(produtoId);

    }

    public void removerPorProduto(Long produtoId) {

        lanceRepository.deleteByProdutoId(produtoId);

    }

}