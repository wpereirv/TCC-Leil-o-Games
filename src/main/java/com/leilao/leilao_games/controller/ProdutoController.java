package com.leilao.leilao_games.controller;

import com.leilao.leilao_games.model.Favorito;
import com.leilao.leilao_games.model.Produto;
import com.leilao.leilao_games.model.Usuario;
import com.leilao.leilao_games.service.AvaliacaoService;
import com.leilao.leilao_games.service.FavoritoService;
import com.leilao.leilao_games.service.LanceService;
import com.leilao.leilao_games.service.ProdutoService;
import com.leilao.leilao_games.service.CategoriaService;
import com.leilao.leilao_games.service.ImagemService;

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

    @Autowired
    private ImagemService imagemService;

    @PostMapping("/anunciar")
public String salvarProduto(
        Produto produto,

        @RequestParam(
                value = "foto1",
                required = false
        )
        MultipartFile foto1,

        @RequestParam(
                value = "foto2",
                required = false
        )
        MultipartFile foto2,

        @RequestParam(
                value = "foto3",
                required = false
        )
        MultipartFile foto3,

        @RequestParam Integer diasLeilao,

        HttpSession session
) {

    Usuario usuario =
            (Usuario) session.getAttribute(
                    "usuarioLogado"
            );

    if (usuario == null) {
        return "redirect:/login";
    }

    if (produto.getNome() == null
            || produto.getNome().isBlank()
            || produto.getNome().length() > 120
            || produto.getDescricao() == null
            || produto.getDescricao().isBlank()
            || produto.getDescricao().length() > 2000
            || produto.getCategoria() == null
            || produto.getCategoria().getId() == null
            || produto.getValorInicial() == null
            || !Double.isFinite(
                    produto.getValorInicial()
            )
            || produto.getValorInicial() <= 0
            || diasLeilao == null) {

        return "redirect:/anunciar?erro=campos";
    }

    try {

        imagemService.validar(foto1);
        imagemService.validar(foto2);
        imagemService.validar(foto3);

        produto.setImagem1(
                imagemService.salvar(foto1)
        );

        produto.setImagem2(
                imagemService.salvar(foto2)
        );

        produto.setImagem3(
                imagemService.salvar(foto3)
        );

    } catch (IOException
             | IllegalArgumentException erro) {

        return "redirect:/anunciar?erro=imagem";
    }

    produto.setNome(
            produto.getNome().trim()
    );

    produto.setDescricao(
            produto.getDescricao().trim()
    );

    if (diasLeilao < 1) {
        diasLeilao = 1;
    }

    if (diasLeilao > 5) {
        diasLeilao = 5;
    }

    LocalDateTime inicio =
            LocalDateTime.now();

    produto.setUsuario(usuario);
    produto.setDataInicio(inicio);
    produto.setDataFim(
            inicio.plusDays(diasLeilao)
    );
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

        @RequestParam(
                value = "foto1",
                required = false
        )
        MultipartFile foto1,

        @RequestParam(
                value = "foto2",
                required = false
        )
        MultipartFile foto2,

        @RequestParam(
                value = "foto3",
                required = false
        )
        MultipartFile foto3,

        @RequestParam(
                value = "removerImagem1",
                defaultValue = "false"
        )
        boolean removerImagem1,

        @RequestParam(
                value = "removerImagem2",
                defaultValue = "false"
        )
        boolean removerImagem2,

        @RequestParam(
                value = "removerImagem3",
                defaultValue = "false"
        )
        boolean removerImagem3,

        HttpSession session) {

    Usuario usuario =
            (Usuario) session.getAttribute(
                    "usuarioLogado"
            );

    if (usuario == null) {
        return "redirect:/login";
    }

    if (produto == null
            || produto.getId() == null) {

        return "redirect:/meus-anuncios";
    }

    Produto produtoBanco =
            produtoService.buscarPorId(
                    produto.getId()
            );

    if (produtoBanco == null
            || produtoBanco.getUsuario() == null
            || !produtoBanco.getUsuario()
                    .getId()
                    .equals(usuario.getId())) {

        return "redirect:/meus-anuncios";
    }

    if (produto.getNome() == null
            || produto.getNome().isBlank()
            || produto.getNome().length() > 120
            || produto.getDescricao() == null
            || produto.getDescricao().isBlank()
            || produto.getDescricao().length() > 2000
            || produto.getCategoria() == null
            || produto.getCategoria().getId() == null
            || produto.getValorInicial() == null
            || !Double.isFinite(
                    produto.getValorInicial()
            )
            || produto.getValorInicial() <= 0) {

        return "redirect:/produto/editar/"
                + produto.getId()
                + "?erro=campos";
    }

    String imagemAntiga1 =
            produtoBanco.getImagem1();

    String imagemAntiga2 =
            produtoBanco.getImagem2();

    String imagemAntiga3 =
            produtoBanco.getImagem3();

    try {

        imagemService.validar(foto1);
        imagemService.validar(foto2);
        imagemService.validar(foto3);

        String imagemNova1 =
                imagemService.salvar(foto1);

        String imagemNova2 =
                imagemService.salvar(foto2);

        String imagemNova3 =
                imagemService.salvar(foto3);

        if (imagemNova1 != null) {
            produtoBanco.setImagem1(imagemNova1);
        } else if (removerImagem1) {
            produtoBanco.setImagem1(null);
        }

        if (imagemNova2 != null) {
            produtoBanco.setImagem2(imagemNova2);
        } else if (removerImagem2) {
            produtoBanco.setImagem2(null);
        }

        if (imagemNova3 != null) {
            produtoBanco.setImagem3(imagemNova3);
        } else if (removerImagem3) {
            produtoBanco.setImagem3(null);
        }

        produtoBanco.setNome(
                produto.getNome().trim()
        );

        produtoBanco.setDescricao(
                produto.getDescricao().trim()
        );

        produtoBanco.setValorInicial(
                produto.getValorInicial()
        );

        produtoBanco.setCategoria(
                produto.getCategoria()
        );

        produtoService.salvar(produtoBanco);

        if ((imagemNova1 != null
                || removerImagem1)
                && imagemAntiga1 != null) {

            imagemService.remover(imagemAntiga1);
        }

        if ((imagemNova2 != null
                || removerImagem2)
                && imagemAntiga2 != null) {

            imagemService.remover(imagemAntiga2);
        }

        if ((imagemNova3 != null
                || removerImagem3)
                && imagemAntiga3 != null) {

            imagemService.remover(imagemAntiga3);
        }

    } catch (IOException
             | IllegalArgumentException erro) {

        return "redirect:/produto/editar/"
                + produto.getId()
                + "?erro=imagem";
    }

    return "redirect:/produto/"
            + produtoBanco.getId();
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