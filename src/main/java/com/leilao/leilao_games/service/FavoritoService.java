package com.leilao.leilao_games.service;

import com.leilao.leilao_games.model.Favorito;
import com.leilao.leilao_games.repository.FavoritoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FavoritoService {

    @Autowired
    private FavoritoRepository favoritoRepository;

    public void salvar(Favorito favorito) {
        favoritoRepository.save(favorito);
    }

    public List<Favorito> buscarPorUsuario(Long usuarioId) {
        return favoritoRepository.findByUsuarioId(usuarioId);
    }

    public long contarFavoritosUsuario(Long usuarioId) {

        return favoritoRepository
                .findByUsuarioId(usuarioId)
                .size();

    }

    public boolean jaExiste(Long usuarioId, Long produtoId) {
        return favoritoRepository.existsByUsuarioIdAndProdutoId(
                usuarioId,
                produtoId);
    }

    public void remover(Long usuarioId, Long produtoId) {

        favoritoRepository.deleteByUsuarioIdAndProdutoId(
                usuarioId,
                produtoId);

    }

    public void removerPorProduto(Long produtoId) {

        favoritoRepository.deleteByProdutoId(produtoId);

    }

}