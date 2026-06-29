package com.leilao.leilao_games.controller;

import com.leilao.leilao_games.model.Favorito;
import com.leilao.leilao_games.model.Lance;
import com.leilao.leilao_games.model.Produto;
import com.leilao.leilao_games.model.Usuario;
import com.leilao.leilao_games.service.AvaliacaoService;
import com.leilao.leilao_games.service.FavoritoService;
import com.leilao.leilao_games.service.LanceService;
import com.leilao.leilao_games.service.ProdutoService;
import com.leilao.leilao_games.service.UsuarioService;
import com.leilao.leilao_games.service.CategoriaService;
import com.leilao.leilao_games.service.CategoriaService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class PageController {

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private LanceService lanceService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private FavoritoService favoritoService;

    @Autowired
    private AvaliacaoService avaliacaoService;

    @Autowired
    private CategoriaService categoriaService;

    @GetMapping("/")
    public String home(
        Model model,
        HttpSession session) {

    Usuario usuario =
            (Usuario) session.getAttribute("usuarioLogado");

    model.addAttribute(
            "usuarioLogado",
            usuario);

    model.addAttribute(
            "produtos",
            produtoService.listarAtivos());

    return "index";
}

    @GetMapping("/login")
    public String login() {
        return "login";
    }

   @GetMapping("/categorias")
   public String categorias(Model model) {

    model.addAttribute(
            "categorias",
            categoriaService.listarTodas());

    return "categorias";
}

    @GetMapping("/leiloes")
public String leiloes(

        @RequestParam(required = false)
        String ordem,

        Model model) {

    model.addAttribute(
            "produtos",
            produtoService.listarOrdenados(ordem));

    model.addAttribute(
            "ordem",
            ordem);

    return "leiloes";
}

    @GetMapping("/colecionaveis")
    public String colecionaveis() {
        return "colecionaveis";
    }

    @GetMapping("/contato")
    public String contato() {
        return "contato";
    }

    @GetMapping("/cadastro")
    public String cadastro() {
        return "cadastro";
    }

    @GetMapping("/dashboard")
public String dashboard(Model model) {

    model.addAttribute(
            "totalUsuarios",
            usuarioService.contarUsuarios()
    );

    model.addAttribute(
            "totalProdutos",
            produtoService.contarProdutos()
    );

    model.addAttribute(
            "produtosAtivos",
            produtoService.contarProdutosAtivos()
    );

    model.addAttribute(
            "produtosEncerrados",
            produtoService.contarProdutosEncerrados()
    );

    model.addAttribute(
            "totalLances",
            lanceService.contarLances()
    );

    model.addAttribute(
            "totalCategorias",
            categoriaService.contarCategorias()
    );

    model.addAttribute(
            "totalAvaliacoes",
            avaliacaoService.contarAvaliacoes()
    );

    return "dashboard";
}

    @GetMapping("/minha-conta")
public String minhaConta(
        HttpSession session,
        Model model) {

    Usuario usuario =
            (Usuario) session.getAttribute("usuarioLogado");

    if (usuario == null) {
        return "redirect:/login";
    }

    model.addAttribute("usuario", usuario);

    model.addAttribute(
            "totalAnuncios",
            produtoService.contarProdutosUsuario(
                    usuario.getId()));

    model.addAttribute(
            "totalLances",
            lanceService.contarLancesUsuario(
                    usuario.getId()));

    model.addAttribute(
            "totalFavoritos",
            favoritoService.contarFavoritosUsuario(
                    usuario.getId()));

    return "minha-conta";
}

    @GetMapping("/anunciar")
public String anunciar(
        Model model,
        HttpSession session) {

    Usuario usuarioLogado =
            (Usuario) session.getAttribute("usuarioLogado");

    if (usuarioLogado == null) {
        return "redirect:/login";
    }

    model.addAttribute(
            "categorias",
            categoriaService.listarTodas()
    );

    return "anunciar";
}

        @GetMapping("/produto/{id}")
        public String detalhesProduto(
        @PathVariable Long id,

        @RequestParam(required = false)
        String erro,

        @RequestParam(required = false)
        String sucesso,

        HttpSession session,

        Model model) {

    Produto produto =
            produtoService.buscarPorId(id);

    Double maiorLance =
            lanceService.buscarMaiorLance(id);

    Usuario usuarioLogado =
            (Usuario) session.getAttribute("usuarioLogado");

    boolean vendedor =
            usuarioLogado != null
            && produto.getUsuario().getId()
                    .equals(usuarioLogado.getId());

        boolean vencedor = false;

        Lance lanceVencedor =
        lanceService.buscarLanceVencedor(id);

        if (usuarioLogado != null && lanceVencedor != null) {

        vencedor =
            lanceVencedor.getUsuario().getId()
                    .equals(usuarioLogado.getId());

}

    model.addAttribute("usuarioLogado", usuarioLogado);

    model.addAttribute("ehVendedor", vendedor);

    model.addAttribute("ehVencedor", vencedor);

    model.addAttribute("produtoAvaliado",
        Boolean.TRUE.equals(produto.getAvaliado()));

    model.addAttribute("erro", erro);
    model.addAttribute("sucesso", sucesso);

    model.addAttribute("produto", produto);
    model.addAttribute("maiorLance", maiorLance);

    model.addAttribute(
            "historicoLances",
            lanceService.buscarPorProduto(id));

    if (Boolean.TRUE.equals(produto.getEncerrado())) {

    model.addAttribute(
            "lanceVencedor",
            lanceVencedor);
}

    model.addAttribute(
            "avaliacoes",
            avaliacaoService.buscarPorVendedor(
                    produto.getUsuario().getId()));

    return "produto";
}

    @GetMapping("/vendedor/{id}")
    public String perfilVendedor(
            @PathVariable Long id,
            Model model) {

        Usuario vendedor =
                usuarioService.buscarPorId(id);

        if (vendedor == null) {
            return "redirect:/";
        }

        model.addAttribute("vendedor", vendedor);

        model.addAttribute(
                "avaliacoes",
                avaliacaoService.buscarPorVendedor(id));

        return "vendedor";
    }

    @GetMapping("/categoria/{id}")
    public String categoria(
            @PathVariable Long id,
            Model model) {

        List<Produto> produtos =
                produtoService.buscarPorCategoria(id);

        model.addAttribute("produtos", produtos);

        return "leiloes";
    }

    @GetMapping("/admin/produtos")
    public String adminProdutos(Model model) {

        model.addAttribute(
                "produtos",
                produtoService.listarTodos());

        return "admin-produtos";
    }

    @GetMapping("/admin/usuarios")
    public String adminUsuarios(Model model) {

        model.addAttribute(
                "usuarios",
                usuarioService.listarTodos());

        return "admin-usuarios";
    }

@PostMapping("/admin/produto/excluir/{id}")
public String excluirProduto(
        @PathVariable Long id,
        RedirectAttributes redirectAttributes) {

    if (!lanceService.buscarPorProduto(id).isEmpty()) {

        redirectAttributes.addFlashAttribute(
                "erro",
                "Este produto não pode ser excluído porque possui lances."
        );

        return "redirect:/admin/produtos";
    }

    try {

        produtoService.excluir(id);

        redirectAttributes.addFlashAttribute(
                "sucesso",
                "Produto excluído com sucesso."
        );

    } catch (Exception erro) {

        redirectAttributes.addFlashAttribute(
                "erro",
                "O produto possui informações vinculadas e não pode ser excluído."
        );
    }

    return "redirect:/admin/produtos";
}

        @PostMapping("/admin/usuario/excluir/{id}")
        public String excluirUsuario(
        @PathVariable Long id,
        HttpSession session,
        RedirectAttributes redirectAttributes) {

    Usuario usuarioLogado =
            (Usuario) session.getAttribute("usuarioLogado");

    if (usuarioLogado != null
            && usuarioLogado.getId().equals(id)) {

        redirectAttributes.addFlashAttribute(
                "erro",
                "Você não pode excluir a própria conta administrativa."
        );

        return "redirect:/admin/usuarios";
    }

    try {

        usuarioService.excluir(id);

        redirectAttributes.addFlashAttribute(
                "sucesso",
                "Usuário excluído com sucesso."
        );

    } catch (Exception erro) {

        redirectAttributes.addFlashAttribute(
                "erro",
                "Este usuário possui produtos, lances ou outras informações vinculadas."
        );
    }

    return "redirect:/admin/usuarios";
}

    @GetMapping("/admin/relatorios")
public String relatorios(Model model) {

    model.addAttribute(
            "totalUsuarios",
            usuarioService.contarUsuarios()
    );

    model.addAttribute(
            "totalProdutos",
            produtoService.contarProdutos()
    );

    model.addAttribute(
            "produtosAtivos",
            produtoService.contarProdutosAtivos()
    );

    model.addAttribute(
            "produtosEncerrados",
            produtoService.contarProdutosEncerrados()
    );

    model.addAttribute(
            "totalLances",
            lanceService.contarLances()
    );

    model.addAttribute(
            "totalCategorias",
            categoriaService.contarCategorias()
    );

    model.addAttribute(
            "totalAvaliacoes",
            avaliacaoService.contarAvaliacoes()
    );

    return "admin-relatorios";
        }

    @GetMapping("/pesquisa")
    public String pesquisar(
            @RequestParam String nome,
            Model model) {

        List<Produto> produtos =
                produtoService.pesquisarPorNome(nome);

        model.addAttribute("produtos", produtos);

        return "leiloes";
    }

    @GetMapping("/meus-anuncios")
    public String meusAnuncios(
            HttpSession session,
            Model model) {

        Usuario usuario =
                (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            return "redirect:/login";
        }

        List<Produto> produtos =
                produtoService.buscarPorUsuario(usuario.getId());

        model.addAttribute("produtos", produtos);

        return "meus-anuncios";
    }

    @GetMapping("/meus-leiloes-ganhos")
        public String meusLeiloesGanhos(
        HttpSession session,
        Model model) {

    Usuario usuario =
            (Usuario) session.getAttribute("usuarioLogado");

    if (usuario == null) {
        return "redirect:/login";
    }

    model.addAttribute(
            "produtos",
            produtoService.buscarPorComprador(
                    usuario.getId()));

    return "meus-leiloes-ganhos";
}

    @GetMapping("/meus-lances")
    public String meusLances(
            HttpSession session,
            Model model) {

        Usuario usuario =
                (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            return "redirect:/login";
        }

        model.addAttribute(
                "lances",
                lanceService.buscarPorUsuario(usuario.getId()));

        return "meus-lances";
    }

    @GetMapping("/favoritos")
    public String favoritos(
            HttpSession session,
            Model model) {

        Usuario usuario =
                (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            return "redirect:/login";
        }

        List<Favorito> favoritos =
                favoritoService.buscarPorUsuario(usuario.getId());

        model.addAttribute("favoritos", favoritos);

        return "favoritos";
    }
}