package com.leilao.leilao_games.controller;

import com.leilao.leilao_games.model.Produto;
import com.leilao.leilao_games.model.Usuario;
import com.leilao.leilao_games.service.ProdutoService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class NegociacaoController {

    @Autowired
    private ProdutoService produtoService;

    @GetMapping("/negociacoes")
    public String listar(
            HttpSession session,
            Model model) {

        Usuario usuario =
                (Usuario) session.getAttribute(
                        "usuarioLogado"
                );

        if (usuario == null) {
            return "redirect:/login";
        }

        List<Produto> negociacoes =
                produtoService.buscarNegociacoes(
                        usuario.getId()
                );

        model.addAttribute(
                "negociacoes",
                negociacoes
        );

        return "negociacoes";
    }

    @PostMapping(
            "/negociacoes/{produtoId}/pagamento"
    )
    public String confirmarPagamento(
            @PathVariable Long produtoId,
            HttpSession session) {

        Usuario usuario = usuario(session);

        if (usuario == null) {
            return "redirect:/login";
        }

        ProdutoService.ResultadoNegociacao resultado =
                produtoService.confirmarPagamento(
                        produtoId,
                        usuario.getId()
                );

        return redirecionar(
                resultado,
                "pagamento"
        );
    }

    @PostMapping(
            "/negociacoes/{produtoId}/envio"
    )
    public String informarEnvio(
            @PathVariable Long produtoId,
            @RequestParam String codigoRastreio,
            HttpSession session) {

        Usuario usuario = usuario(session);

        if (usuario == null) {
            return "redirect:/login";
        }

        ProdutoService.ResultadoNegociacao resultado =
                produtoService.informarEnvio(
                        produtoId,
                        usuario.getId(),
                        codigoRastreio
                );

        return redirecionar(
                resultado,
                "envio"
        );
    }

    @PostMapping(
            "/negociacoes/{produtoId}/recebimento"
    )
    public String confirmarRecebimento(
            @PathVariable Long produtoId,
            HttpSession session) {

        Usuario usuario = usuario(session);

        if (usuario == null) {
            return "redirect:/login";
        }

        ProdutoService.ResultadoNegociacao resultado =
                produtoService.confirmarRecebimento(
                        produtoId,
                        usuario.getId()
                );

        return redirecionar(
                resultado,
                "recebimento"
        );
    }

    private Usuario usuario(HttpSession session) {

        return (Usuario) session.getAttribute(
                "usuarioLogado"
        );
    }

    private String redirecionar(
            ProdutoService.ResultadoNegociacao resultado,
            String sucesso) {

        if (resultado
                == ProdutoService
                .ResultadoNegociacao
                .SUCESSO) {

            return "redirect:/negociacoes?sucesso="
                    + sucesso;
        }

        return "redirect:/negociacoes?erro="
                + resultado.name().toLowerCase();
    }
}