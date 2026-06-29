package com.leilao.leilao_games.controller;

import com.leilao.leilao_games.model.Lance;
import com.leilao.leilao_games.model.Produto;
import com.leilao.leilao_games.model.Usuario;
import com.leilao.leilao_games.service.LanceService;
import com.leilao.leilao_games.service.ProdutoService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.leilao.leilao_games.service.NotificacaoService;

@Controller
public class LanceController {

    @Autowired
    private LanceService lanceService;

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private NotificacaoService notificacaoService;

    @PostMapping("/lance")
    public String registrarLance(

            @RequestParam Long produtoId,

            @RequestParam Double valor,

            HttpSession session) {

        Usuario usuario =
                (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            return "redirect:/login";
        }

        Produto produto =
                produtoService.buscarPorId(produtoId);

        if (produto == null) {
            return "redirect:/leiloes";
        }

        if (Boolean.TRUE.equals(produto.getEncerrado())) {
            return "redirect:/produto/" + produtoId + "?erro=encerrado";
        }

        if (produto.getUsuario().getId().equals(usuario.getId())) {
            return "redirect:/produto/" + produtoId + "?erro=vendedorLance";
        }

        Double maiorLance =
                lanceService.buscarMaiorLance(produtoId);

        if (maiorLance == null || maiorLance <= 0) {

            if (valor < produto.getValorInicial()) {

                return "redirect:/produto/" + produtoId
                        + "?erro=valorInicial";
            }

        } else {

            if (valor <= maiorLance) {

                return "redirect:/produto/" + produtoId
                        + "?erro=lanceMenor";
            }
        }

        Lance lance = new Lance();

        lance.setValor(valor);
        lance.setProduto(produto);
        lance.setUsuario(usuario);

        lanceService.salvar(lance);

    notificacaoService.criar(
        produto.getUsuario(),
        "LANCE",
        usuario.getNome()
                + " deu um lance de R$ "
                + String.format("%.2f", valor)
                + " no produto "
                + produto.getNome()
                + ".",
        "/produto/" + produtoId
    );

    return "redirect:/produto/" + produtoId + "?sucesso=lance";
    }
}