package com.leilao.leilao_games.service;

import com.leilao.leilao_games.model.Lance;
import com.leilao.leilao_games.model.Produto;
import com.leilao.leilao_games.repository.ProdutoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private LanceService lanceService;

    @Autowired
    private NotificacaoService notificacaoService;

    public Produto salvar(Produto produto) {
        return produtoRepository.save(produto);
    }

    public List<Produto> listarTodos() {
        return produtoRepository.findAll();
    }

   public Produto buscarPorId(Long id) {

    Produto produto = produtoRepository
            .findById(id)
            .orElse(null);

    if (produto != null
            && produto.getDataFim() != null
            && LocalDateTime.now()
                    .isAfter(produto.getDataFim())
            && !Boolean.TRUE.equals(
                    produto.getEncerrado())) {

        produto.setEncerrado(true);

        Lance lanceVencedor =
                lanceService.buscarLanceVencedor(id);

        if (lanceVencedor != null) {

            produto.setComprador(
                    lanceVencedor.getUsuario()
            );
        }

        produtoRepository.save(produto);

        notificacaoService.criar(
                produto.getUsuario(),
                "LEILAO_ENCERRADO",
                "Seu leilão do produto "
                        + produto.getNome()
                        + " foi encerrado.",
                "/produto/" + produto.getId()
        );

        if (lanceVencedor != null) {

            notificacaoService.criar(
                    lanceVencedor.getUsuario(),
                    "LEILAO_VENCIDO",
                    "Parabéns! Você venceu o leilão do produto "
                            + produto.getNome()
                            + ".",
                    "/produto/" + produto.getId()
            );
        }
    }

    return produto;
}

    public List<Produto> buscarPorCategoria(Long categoriaId) {
        return produtoRepository.findByCategoriaId(categoriaId);
    }

    public long contarProdutos() {
        return produtoRepository.count();
    }

    public long contarProdutosUsuario(Long usuarioId) {
        return produtoRepository.countByUsuarioId(usuarioId);
    }

    public List<Produto> buscarPorComprador(Long compradorId) {
        return produtoRepository.findByCompradorId(compradorId);
    }

    public void excluir(Long id) {
        produtoRepository.deleteById(id);
    }

    public List<Produto> pesquisarPorNome(String nome) {
        return produtoRepository.findByNomeContainingIgnoreCase(nome);
    }

    public List<Produto> buscarPorUsuario(Long usuarioId) {
        return produtoRepository.findByUsuarioId(usuarioId);
    }

    public List<Produto> listarAtivos() {
        return produtoRepository.findByEncerradoFalse();
    }

    public List<Produto> listarEncerrados() {
        return produtoRepository.findByEncerradoTrue();
    }

    public List<Produto> listarOrdenados(String ordem) {

        if (ordem == null || ordem.isBlank()) {
            return produtoRepository.findByEncerradoFalseOrderByIdDesc();
        }

        switch (ordem) {

            case "menorPreco":
                return produtoRepository.findByEncerradoFalseOrderByValorInicialAsc();

            case "maiorPreco":
                return produtoRepository.findByEncerradoFalseOrderByValorInicialDesc();

            case "terminando":
                return produtoRepository.findByEncerradoFalseOrderByDataFimAsc();

            default:
                return produtoRepository.findByEncerradoFalseOrderByIdDesc();
        }
    }

    public void verificarLeiloesEncerrados() {

    List<Produto> produtosVencidos =
            produtoRepository
                    .findByEncerradoFalseAndDataFimBefore(
                            LocalDateTime.now()
                    );

    for (Produto produto : produtosVencidos) {
        buscarPorId(produto.getId());
    }
    }

    public long contarProdutosAtivos() {

    return produtoRepository
            .countByEncerradoFalse();
}

public long contarProdutosEncerrados() {

    return produtoRepository
            .countByEncerradoTrue();
    }

}