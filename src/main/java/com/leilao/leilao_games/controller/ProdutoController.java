package com.leilao.leilao_games.controller;

import com.leilao.leilao_games.model.Favorito;
import com.leilao.leilao_games.model.Produto;
import com.leilao.leilao_games.model.Usuario;
import com.leilao.leilao_games.service.AvaliacaoService;
import com.leilao.leilao_games.service.FavoritoService;
import com.leilao.leilao_games.service.LanceService;
import com.leilao.leilao_games.service.ProdutoService;
import com.leilao.leilao_games.service.CategoriaService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

@Controller
public class ProdutoController {

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private FavoritoService favoritoService;

    @Autowired
    private LanceService lanceService;

    @Autowired
    private AvaliacaoService avaliacaoService;

    @Autowired
    private CategoriaService categoriaService;

    @PostMapping("/anunciar")
    public String salvarProduto(
            Produto produto,

            @RequestParam("foto1") MultipartFile foto1,

            @RequestParam("foto2") MultipartFile foto2,

            @RequestParam("foto3") MultipartFile foto3,

            @RequestParam Integer diasLeilao,

            HttpSession session
    ) throws IOException {

        Usuario usuario =
                (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            return "redirect:/login";
        }

        Path caminhoUploads =
                Paths.get("src/main/resources/static/uploads");

        if (!Files.exists(caminhoUploads)) {
            Files.createDirectories(caminhoUploads);
        }

        if (!foto1.isEmpty()) {

            String nomeFoto1 = foto1.getOriginalFilename();

            foto1.transferTo(caminhoUploads.resolve(nomeFoto1));

            produto.setImagem1(nomeFoto1);
        }

        if (!foto2.isEmpty()) {

            String nomeFoto2 = foto2.getOriginalFilename();

            foto2.transferTo(caminhoUploads.resolve(nomeFoto2));

            produto.setImagem2(nomeFoto2);
        }

        if (!foto3.isEmpty()) {

            String nomeFoto3 = foto3.getOriginalFilename();

            foto3.transferTo(caminhoUploads.resolve(nomeFoto3));

            produto.setImagem3(nomeFoto3);
        }

        if (diasLeilao < 1) {
            diasLeilao = 1;
        }

        if (diasLeilao > 5) {
            diasLeilao = 5;
        }

        LocalDateTime inicio = LocalDateTime.now();

        produto.setUsuario(usuario);

        produto.setDataInicio(inicio);

        produto.setDataFim(inicio.plusDays(diasLeilao));

        produto.setEncerrado(false);

        produtoService.salvar(produto);

        return "redirect:/minha-conta";
    }

    @GetMapping("/produto/editar/{id}")
    public String editarProduto(
            @PathVariable Long id,
            HttpSession session,
            Model model) {

        Usuario usuario =
                (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            return "redirect:/login";
        }

        Produto produto =
                produtoService.buscarPorId(id);

        if (produto == null) {
            return "redirect:/meus-anuncios";
        }

        if (!produto.getUsuario().getId().equals(usuario.getId())) {
            return "redirect:/meus-anuncios";
        }

        model.addAttribute("produto", produto);

model.addAttribute(
        "categorias",
        categoriaService.listarTodas()
);

return "editar-produto";
    }

    @PostMapping("/produto/editar")
    public String salvarEdicao(
            Produto produto,
            HttpSession session) {

        Usuario usuario =
                (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            return "redirect:/login";
        }

        Produto produtoBanco =
                produtoService.buscarPorId(produto.getId());

        if (produtoBanco == null) {
            return "redirect:/meus-anuncios";
        }

        if (!produtoBanco.getUsuario().getId().equals(usuario.getId())) {
            return "redirect:/meus-anuncios";
        }

        produtoBanco.setNome(produto.getNome());
        produtoBanco.setDescricao(produto.getDescricao());
        produtoBanco.setValorInicial(produto.getValorInicial());
        produtoBanco.setCategoria(produto.getCategoria());

        produtoService.salvar(produtoBanco);

        return "redirect:/produto/" + produtoBanco.getId();
    }

    @PostMapping("/produto/excluir/{id}")
public String excluirProduto(
        @PathVariable Long id,
        HttpSession session,
        RedirectAttributes redirectAttributes) {

    Usuario usuario =
            (Usuario) session.getAttribute("usuarioLogado");

    if (usuario == null) {
        return "redirect:/login";
    }

    Produto produto =
            produtoService.buscarPorId(id);

    if (produto == null) {

        redirectAttributes.addFlashAttribute(
                "erro",
                "Produto não encontrado."
        );

        return "redirect:/meus-anuncios";
    }

    if (!produto.getUsuario()
            .getId()
            .equals(usuario.getId())) {

        redirectAttributes.addFlashAttribute(
                "erro",
                "Você não pode excluir este produto."
        );

        return "redirect:/meus-anuncios";
    }

    if (!lanceService.buscarPorProduto(id).isEmpty()) {

        redirectAttributes.addFlashAttribute(
                "erro",
                "Este anúncio não pode ser excluído porque possui lances."
        );

        return "redirect:/meus-anuncios";
    }

    try {

        favoritoService.removerPorProduto(id);

        avaliacaoService.removerPorProduto(id);

        produtoService.excluir(id);

        redirectAttributes.addFlashAttribute(
                "sucesso",
                "Anúncio excluído com sucesso."
        );

    } catch (Exception erro) {

        redirectAttributes.addFlashAttribute(
                "erro",
                "O anúncio possui informações vinculadas e não pode ser excluído."
        );
    }

    return "redirect:/meus-anuncios";
}

    @PostMapping("/favoritar")
    public String favoritarProduto(
            @RequestParam Long produtoId,
            HttpSession session) {

        Usuario usuario =
                (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            return "redirect:/login";
        }

        Produto produto = produtoService.buscarPorId(produtoId);

        if (produto.getUsuario().getId().equals(usuario.getId())) {
        return "redirect:/produto/" + produtoId;
        }

        if (!favoritoService.jaExiste(
                usuario.getId(),
                produto.getId())) {

            Favorito favorito = new Favorito();

            favorito.setUsuario(usuario);

            favorito.setProduto(produto);

            favoritoService.salvar(favorito);
        }

        return "redirect:/produto/" + produtoId;
    }

    @PostMapping("/remover-favorito")
    public String removerFavorito(
            @RequestParam Long produtoId,
            HttpSession session) {

        Usuario usuario =
                (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            return "redirect:/login";
        }

        favoritoService.remover(
                usuario.getId(),
                produtoId);

        return "redirect:/favoritos";
    }
}