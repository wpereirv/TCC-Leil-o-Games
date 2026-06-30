package com.leilao.leilao_games.service;

import com.leilao.leilao_games.model.Lance;
import com.leilao.leilao_games.model.Produto;
import com.leilao.leilao_games.repository.ProdutoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.leilao.leilao_games.model.StatusNegociacao;
import org.springframework.transaction.annotation.Transactional;

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

    produto.setValorFinal(
            lanceVencedor.getValor()
    );

    produto.setStatusNegociacao(
            StatusNegociacao.AGUARDANDO_PAGAMENTO
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

    public enum ResultadoNegociacao {
    SUCESSO,
    NAO_ENCONTRADA,
    NAO_AUTORIZADO,
    STATUS_INVALIDO,
    CODIGO_INVALIDO
    }

    @Transactional
public List<Produto> buscarNegociacoes(
        Long usuarioId) {

    List<Produto> encerrados =
            produtoRepository.findByEncerradoTrue();

    for (Produto produto : encerrados) {

        if (produto.getStatusNegociacao() != null) {
            continue;
        }

        Lance lanceVencedor =
                lanceService.buscarLanceVencedor(
                        produto.getId()
                );

        if (lanceVencedor == null) {
            continue;
        }

        produto.setComprador(
                lanceVencedor.getUsuario()
        );

        produto.setValorFinal(
                lanceVencedor.getValor()
        );

        produto.setStatusNegociacao(
                StatusNegociacao.AGUARDANDO_PAGAMENTO
        );

        produtoRepository.save(produto);
    }

    return produtoRepository
            .buscarNegociacoesDoUsuario(usuarioId);
}

@Transactional
public ResultadoNegociacao confirmarPagamento(
        Long produtoId,
        Long usuarioId) {

    Produto produto =
            produtoRepository
                    .buscarPorIdComBloqueio(produtoId)
                    .orElse(null);

    if (produto == null) {
        return ResultadoNegociacao.NAO_ENCONTRADA;
    }

    if (produto.getComprador() == null
            || !produto.getComprador()
                    .getId()
                    .equals(usuarioId)) {

        return ResultadoNegociacao.NAO_AUTORIZADO;
    }

    if (produto.getStatusNegociacao()
            != StatusNegociacao.AGUARDANDO_PAGAMENTO) {

        return ResultadoNegociacao.STATUS_INVALIDO;
    }

    produto.setStatusNegociacao(
            StatusNegociacao.AGUARDANDO_ENVIO
    );

    produto.setDataPagamento(
            LocalDateTime.now()
    );

    produtoRepository.save(produto);

    notificacaoService.criar(
            produto.getUsuario(),
            "NEGOCIACAO",
            "O pagamento do produto "
                    + produto.getNome()
                    + " foi confirmado.",
            "/negociacoes"
    );

    return ResultadoNegociacao.SUCESSO;
}

@Transactional
public ResultadoNegociacao informarEnvio(
        Long produtoId,
        Long usuarioId,
        String codigoRastreio) {

    Produto produto =
            produtoRepository
                    .buscarPorIdComBloqueio(produtoId)
                    .orElse(null);

    if (produto == null) {
        return ResultadoNegociacao.NAO_ENCONTRADA;
    }

    if (produto.getUsuario() == null
            || !produto.getUsuario()
                    .getId()
                    .equals(usuarioId)) {

        return ResultadoNegociacao.NAO_AUTORIZADO;
    }

    if (produto.getStatusNegociacao()
            != StatusNegociacao.AGUARDANDO_ENVIO) {

        return ResultadoNegociacao.STATUS_INVALIDO;
    }

    if (codigoRastreio == null
            || codigoRastreio.isBlank()
            || codigoRastreio.length() > 100) {

        return ResultadoNegociacao.CODIGO_INVALIDO;
    }

    produto.setCodigoRastreio(
            codigoRastreio.trim()
    );

    produto.setDataEnvio(
            LocalDateTime.now()
    );

    produto.setStatusNegociacao(
            StatusNegociacao.EM_TRANSPORTE
    );

    produtoRepository.save(produto);

    notificacaoService.criar(
            produto.getComprador(),
            "NEGOCIACAO",
            "O produto "
                    + produto.getNome()
                    + " foi enviado.",
            "/negociacoes"
    );

    return ResultadoNegociacao.SUCESSO;
}

@Transactional
public ResultadoNegociacao confirmarRecebimento(
        Long produtoId,
        Long usuarioId) {

    Produto produto =
            produtoRepository
                    .buscarPorIdComBloqueio(produtoId)
                    .orElse(null);

    if (produto == null) {
        return ResultadoNegociacao.NAO_ENCONTRADA;
    }

    if (produto.getComprador() == null
            || !produto.getComprador()
                    .getId()
                    .equals(usuarioId)) {

        return ResultadoNegociacao.NAO_AUTORIZADO;
    }

    if (produto.getStatusNegociacao()
            != StatusNegociacao.EM_TRANSPORTE) {

        return ResultadoNegociacao.STATUS_INVALIDO;
    }

    produto.setStatusNegociacao(
            StatusNegociacao.CONCLUIDA
    );

    produto.setDataConclusao(
            LocalDateTime.now()
    );

    produtoRepository.save(produto);

    notificacaoService.criar(
            produto.getUsuario(),
            "NEGOCIACAO",
            "A entrega do produto "
                    + produto.getNome()
                    + " foi confirmada.",
            "/negociacoes"
    );

    return ResultadoNegociacao.SUCESSO;
}

}