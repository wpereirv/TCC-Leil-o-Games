package com.leilao.leilao_games.controller;

import com.leilao.leilao_games.model.Avaliacao;
import com.leilao.leilao_games.model.Lance;
import com.leilao.leilao_games.model.Produto;
import com.leilao.leilao_games.model.Usuario;
import com.leilao.leilao_games.service.AvaliacaoService;
import com.leilao.leilao_games.service.LanceService;
import com.leilao.leilao_games.service.ProdutoService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.dao.DataIntegrityViolationException;

@Controller
public class AvaliacaoController {

    @Autowired
    private AvaliacaoService avaliacaoService;

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private LanceService lanceService;

    @PostMapping("/avaliar")
    public String avaliarVendedor(

            @RequestParam Long produtoId,

            @RequestParam Integer nota,

            @RequestParam String comentario,

            HttpSession session) {

        Usuario comprador =
                (Usuario) session.getAttribute("usuarioLogado");

        if (comprador == null) {

    return "redirect:/login";

}

if (produtoId == null
        || nota == null
        || nota < 1
        || nota > 5
        || comentario == null
        || comentario.isBlank()
        || comentario.length() > 1000) {

    return "redirect:/produto/"
            + produtoId
            + "?erro=avaliacaoInvalida";
}

comentario = comentario.trim();

Produto produto =
        produtoService.buscarPorId(produtoId);

        if (produto == null) {

            return "redirect:/leiloes";

        }

        if (!Boolean.TRUE.equals(produto.getEncerrado())) {

            return "redirect:/produto/" + produtoId
                    + "?erro=leilaoNaoEncerrado";

        }

        Lance vencedor =
                lanceService.buscarLanceVencedor(produtoId);

        if (vencedor == null) {

            return "redirect:/produto/" + produtoId
                    + "?erro=semVencedor";

        }

        if (!vencedor.getUsuario().getId().equals(comprador.getId())) {

            return "redirect:/produto/" + produtoId
                    + "?erro=naoVencedor";

        }

        if (produto.getUsuario().getId().equals(comprador.getId())) {

           return "redirect:/produto/" + produtoId
                  + "?erro=avaliacaoVendedor";

        }

        if (avaliacaoService.produtoJaAvaliado(produtoId)) {

            return "redirect:/produto/" + produtoId
                    + "?erro=jaAvaliado";

        }

        Avaliacao avaliacao = new Avaliacao();

        avaliacao.setProduto(produto);

        avaliacao.setComprador(comprador);

        avaliacao.setVendedor(produto.getUsuario());

        avaliacao.setNota(nota);

        avaliacao.setComentario(comentario);

        try {

    avaliacaoService.salvar(avaliacao);

    produto.setAvaliado(true);

    produtoService.salvar(produto);

} 
        catch (DataIntegrityViolationException
                | IllegalArgumentException erro) {

        return "redirect:/produto/"
            + produtoId
            + "?erro=jaAvaliado";
}

        return "redirect:/produto/" + produtoId
                + "?sucesso=avaliacao";

    }

}