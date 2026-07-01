package com.leilao.leilao_games.controller;

import com.leilao.leilao_games.service.AvaliacaoService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AdminAvaliacaoController {

    private final AvaliacaoService avaliacaoService;

    @GetMapping("/admin/avaliacoes")
    public String listar(Model model) {

        model.addAttribute(
                "avaliacoes",
                avaliacaoService.listarTodas()
        );

        model.addAttribute(
                "totalAvaliacoes",
                avaliacaoService.contarAvaliacoes()
        );

        return "admin-avaliacoes";
    }

    @PostMapping("/admin/avaliacoes/excluir/{id}")
    public String excluir(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {

            avaliacaoService.excluir(id);

            redirectAttributes.addFlashAttribute(
                    "sucesso",
                    "Avaliação removida com sucesso."
            );

        } catch (IllegalArgumentException erro) {

            redirectAttributes.addFlashAttribute(
                    "erro",
                    erro.getMessage()
            );

        } catch (Exception erro) {

            redirectAttributes.addFlashAttribute(
                    "erro",
                    "Não foi possível remover a avaliação."
            );
        }

        return "redirect:/admin/avaliacoes";
    }
}