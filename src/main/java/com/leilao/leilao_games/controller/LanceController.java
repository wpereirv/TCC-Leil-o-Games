package com.leilao.leilao_games.controller;

import com.leilao.leilao_games.model.Produto;
import com.leilao.leilao_games.model.Usuario;
import com.leilao.leilao_games.service.LanceService;
import com.leilao.leilao_games.service.NotificacaoService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LanceController {

    @Autowired
    private LanceService lanceService;

    @Autowired
    private NotificacaoService notificacaoService;

    @PostMapping("/lance")
    public String registrarLance(
            @RequestParam Long produtoId,
            @RequestParam Double valor,
            HttpSession session) {

        Usuario usuario =
                (Usuario) session.getAttribute(
                        "usuarioLogado"
                );

        if (usuario == null) {
            return "redirect:/login";
        }

        LanceService.Registro registro =
                lanceService.registrar(
                        produtoId,
                        valor,
                        usuario
                );

        LanceService.Resultado resultado =
                registro.resultado();

        if (resultado
                == LanceService.Resultado
                        .PRODUTO_INEXISTENTE
                || resultado
                == LanceService.Resultado
                        .VALOR_INVALIDO) {

            return "redirect:/leiloes";
        }

        if (resultado
                == LanceService.Resultado
                        .ENCERRADO) {

            return "redirect:/produto/"
                    + produtoId
                    + "?erro=encerrado";
        }

        if (resultado
                == LanceService.Resultado
                        .VENDEDOR) {

            return "redirect:/produto/"
                    + produtoId
                    + "?erro=vendedor";
        }

        if (resultado
                == LanceService.Resultado
                        .VALOR_INICIAL) {

            return "redirect:/produto/"
                    + produtoId
                    + "?erro=valorInicial";
        }

        if (resultado
                == LanceService.Resultado
                        .LANCE_MENOR) {

            return "redirect:/produto/"
                    + produtoId
                    + "?erro=lanceMenor";
        }

        Produto produto = registro.produto();

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

        return "redirect:/produto/"
                + produtoId
                + "?sucesso=lance";
    }
}