package com.leilao.leilao_games.controller;

import com.leilao.leilao_games.model.Categoria;
import com.leilao.leilao_games.service.CategoriaService;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AdminCategoriaController {

    private final CategoriaService categoriaService;

    @GetMapping("/admin/categorias")
    public String listar(Model model) {

        model.addAttribute(
                "categorias",
                categoriaService.listarTodas()
        );

        model.addAttribute(
                "categoriaForm",
                new Categoria()
        );

        return "admin-categorias";
    }

    @GetMapping("/admin/categorias/editar/{id}")
    public String editar(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        Categoria categoria =
                categoriaService.buscarPorId(id);

        if (categoria == null) {

            redirectAttributes.addFlashAttribute(
                    "erro",
                    "Categoria não encontrada."
            );

            return "redirect:/admin/categorias";
        }

        model.addAttribute(
                "categorias",
                categoriaService.listarTodas()
        );

        model.addAttribute(
                "categoriaForm",
                categoria
        );

        return "admin-categorias";
    }

    @PostMapping("/admin/categorias/salvar")
    public String salvar(
            Categoria categoria,
            RedirectAttributes redirectAttributes) {

        try {

            categoriaService.salvar(categoria);

            redirectAttributes.addFlashAttribute(
                    "sucesso",
                    "Categoria salva com sucesso."
            );

        } catch (IllegalArgumentException erro) {

            redirectAttributes.addFlashAttribute(
                    "erro",
                    erro.getMessage()
            );
        }

        return "redirect:/admin/categorias";
    }

    @PostMapping("/admin/categorias/excluir/{id}")
    public String excluir(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {

            categoriaService.excluir(id);

            redirectAttributes.addFlashAttribute(
                    "sucesso",
                    "Categoria excluída com sucesso."
            );

        } catch (DataIntegrityViolationException erro) {

            redirectAttributes.addFlashAttribute(
                    "erro",
                    "Não é possível excluir uma categoria que possui produtos."
            );

        } catch (Exception erro) {

            redirectAttributes.addFlashAttribute(
                    "erro",
                    "Não foi possível excluir a categoria."
            );
        }

        return "redirect:/admin/categorias";
    }
}