package com.leilao.leilao_games.service;

import com.leilao.leilao_games.model.Conversa;
import com.leilao.leilao_games.model.Produto;
import com.leilao.leilao_games.model.Usuario;
import com.leilao.leilao_games.repository.ConversaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConversaService {

    private final ConversaRepository conversaRepository;

    public Conversa buscarOuCriar(
            Produto produto,
            Usuario comprador) {

        return conversaRepository
                .findByProdutoIdAndCompradorId(
                        produto.getId(),
                        comprador.getId()
                )
                .orElseGet(() -> {
                    Conversa conversa = new Conversa();

                    conversa.setProduto(produto);
                    conversa.setComprador(comprador);
                    conversa.setVendedor(produto.getUsuario());

                    return conversaRepository.save(conversa);
                });
    }

    public Conversa buscarPorId(Long id) {
        return conversaRepository.findById(id).orElse(null);
    }

    public List<Conversa> buscarPorComprador(Long compradorId) {
        return conversaRepository.findByCompradorId(compradorId);
    }

    public List<Conversa> buscarPorVendedor(Long vendedorId) {
        return conversaRepository.findByVendedorId(vendedorId);
    }

    public List<Conversa> buscarPorUsuario(Long usuarioId) {

        List<Conversa> conversas = new ArrayList<>();

        conversas.addAll(
                conversaRepository.findByCompradorId(usuarioId)
        );

        conversas.addAll(
                conversaRepository.findByVendedorId(usuarioId)
        );

      conversas.sort(
        Comparator.comparingLong((Conversa conversa) -> {
            Long id = conversa.getId();
            return id == null ? Long.MIN_VALUE : id;
        }).reversed()
);

        return conversas;
    }
}