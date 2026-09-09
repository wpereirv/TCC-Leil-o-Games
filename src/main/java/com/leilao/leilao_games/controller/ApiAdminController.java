package com.leilao.leilao_games.controller;

import com.leilao.leilao_games.dto.AdminAvaliacaoDTO;
import com.leilao.leilao_games.dto.AdminProdutoDTO;
import com.leilao.leilao_games.dto.CategoriaDTO;
import com.leilao.leilao_games.dto.CategoriaRequestDTO;
import com.leilao.leilao_games.dto.UsuarioDTO;
import com.leilao.leilao_games.model.Categoria;
import com.leilao.leilao_games.model.Produto;
import com.leilao.leilao_games.model.Usuario;
import com.leilao.leilao_games.service.AvaliacaoService;
import com.leilao.leilao_games.service.CategoriaService;
import com.leilao.leilao_games.service.LanceService;
import com.leilao.leilao_games.service.ProdutoService;
import com.leilao.leilao_games.service.UsuarioService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@CrossOrigin(
        origins = "${app.cors.allowed-origin}",
        allowCredentials = "true"
)
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class ApiAdminController {

    private final UsuarioService usuarioService;
    private final ProdutoService produtoService;
    private final LanceService lanceService;
    private final CategoriaService categoriaService;
    private final AvaliacaoService avaliacaoService;

    @GetMapping("/resumo")
    public ResponseEntity<?> resumo(
            HttpServletRequest request) {

        if (buscarUsuarioLogado(request) == null) {
            return naoAutenticado();
        }

        return ResponseEntity.ok(Map.of(
                "totalUsuarios",
                usuarioService.contarUsuarios(),
                "totalProdutos",
                produtoService.contarProdutos(),
                "produtosAtivos",
                produtoService.contarProdutosAtivos(),
                "produtosEncerrados",
                produtoService.contarProdutosEncerrados(),
                "totalLances",
                lanceService.contarLances(),
                "totalCategorias",
                categoriaService.contarCategorias(),
                "totalAvaliacoes",
                avaliacaoService.contarAvaliacoes()
        ));
    }

    @GetMapping("/usuarios")
    public ResponseEntity<?> listarUsuarios(
            HttpServletRequest request) {

        if (buscarUsuarioLogado(request) == null) {
            return naoAutenticado();
        }

        List<UsuarioDTO> usuarios =
                usuarioService.listarTodos()
                        .stream()
                        .map(UsuarioDTO::de)
                        .toList();

        return ResponseEntity.ok(usuarios);
    }

    @PutMapping("/usuarios/{usuarioId}/status/{ativo}")
    public ResponseEntity<?> atualizarStatusUsuario(
            @PathVariable Long usuarioId,
            @PathVariable boolean ativo,
            HttpServletRequest request) {

        Usuario usuarioLogado =
                buscarUsuarioLogado(request);

        if (usuarioLogado == null) {
            return naoAutenticado();
        }

        if (!ativo
                && usuarioLogado.getId().equals(usuarioId)) {

            return ResponseEntity.badRequest().body(Map.of(
                    "erro",
                    "Você não pode desativar a própria conta."
            ));
        }

        Usuario usuario = usuarioService
                .buscarPorIdInclusoInativo(usuarioId);

        if (usuario == null) {
            return ResponseEntity.notFound().build();
        }

        usuarioService.atualizarStatus(usuarioId, ativo);

        if (!ativo) {
            produtoService
                    .desativarLeiloesAtivosDoUsuario(usuarioId);
        }

        return ResponseEntity.ok(Map.of(
                "mensagem",
                ativo
                        ? "Usuário reativado com sucesso."
                        : "Usuário desativado. Os leilões ativos dele também foram desativados."
        ));
    }

    @GetMapping("/produtos")
    public ResponseEntity<?> listarProdutos(
            HttpServletRequest request) {

        if (buscarUsuarioLogado(request) == null) {
            return naoAutenticado();
        }

        List<AdminProdutoDTO> produtos =
                produtoService.listarTodos()
                        .stream()
                        .map(produto -> AdminProdutoDTO.de(
                                produto,
                                lanceService
                                        .buscarPorProduto(
                                                produto.getId()
                                        )
                                        .size()
                        ))
                        .toList();

        return ResponseEntity.ok(produtos);
    }

    @PutMapping("/produtos/{produtoId}/status/{ativo}")
    public ResponseEntity<?> atualizarStatusProduto(
            @PathVariable Long produtoId,
            @PathVariable boolean ativo,
            HttpServletRequest request) {

        if (buscarUsuarioLogado(request) == null) {
            return naoAutenticado();
        }

        Produto produto = produtoService
                .buscarPorIdInclusoInativo(produtoId);

        if (produto == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            produtoService.atualizarStatus(produtoId, ativo);

            return ResponseEntity.ok(Map.of(
                    "mensagem",
                    ativo
                            ? "Leilão reativado com sucesso."
                            : "Leilão desativado com sucesso."
            ));
        } catch (IllegalStateException erro) {
            return ResponseEntity.badRequest().body(Map.of(
                    "erro",
                    erro.getMessage()
            ));
        }
    }

    @GetMapping("/categorias")
    public ResponseEntity<?> listarCategorias(
            HttpServletRequest request) {

        if (buscarUsuarioLogado(request) == null) {
            return naoAutenticado();
        }

        List<CategoriaDTO> categorias =
                categoriaService.listarTodas()
                        .stream()
                        .map(CategoriaDTO::de)
                        .toList();

        return ResponseEntity.ok(categorias);
    }

    @PostMapping("/categorias")
    public ResponseEntity<?> criarCategoria(
            @RequestBody CategoriaRequestDTO dados,
            HttpServletRequest request) {

        if (buscarUsuarioLogado(request) == null) {
            return naoAutenticado();
        }

        try {
            Categoria categoria = new Categoria();

            categoria.setNome(dados.nome());
            categoria.setDescricao(dados.descricao());

            Categoria salva =
                    categoriaService.salvar(categoria);

            return ResponseEntity.ok(CategoriaDTO.de(salva));
        } catch (IllegalArgumentException erro) {
            return ResponseEntity.badRequest().body(Map.of(
                    "erro",
                    erro.getMessage()
            ));
        }
    }

    @PutMapping("/categorias/{categoriaId}")
    public ResponseEntity<?> editarCategoria(
            @PathVariable Long categoriaId,
            @RequestBody CategoriaRequestDTO dados,
            HttpServletRequest request) {

        if (buscarUsuarioLogado(request) == null) {
            return naoAutenticado();
        }

        Categoria categoria =
                categoriaService.buscarPorId(categoriaId);

        if (categoria == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            categoria.setNome(dados.nome());
            categoria.setDescricao(dados.descricao());

            Categoria salva =
                    categoriaService.salvar(categoria);

            return ResponseEntity.ok(CategoriaDTO.de(salva));
        } catch (IllegalArgumentException erro) {
            return ResponseEntity.badRequest().body(Map.of(
                    "erro",
                    erro.getMessage()
            ));
        }
    }

    @DeleteMapping("/categorias/{categoriaId}")
    public ResponseEntity<?> excluirCategoria(
            @PathVariable Long categoriaId,
            HttpServletRequest request) {

        if (buscarUsuarioLogado(request) == null) {
            return naoAutenticado();
        }

        Categoria categoria =
                categoriaService.buscarPorId(categoriaId);

        if (categoria == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            categoriaService.excluir(categoriaId);

            return ResponseEntity.ok(Map.of(
                    "mensagem",
                    "Categoria excluída com sucesso."
            ));
        } catch (DataIntegrityViolationException erro) {
            return ResponseEntity.badRequest().body(Map.of(
                    "erro",
                    "Não é possível excluir uma categoria que possui produtos."
            ));
        }
    }

    @GetMapping("/avaliacoes")
    public ResponseEntity<?> listarAvaliacoes(
            HttpServletRequest request) {

        if (buscarUsuarioLogado(request) == null) {
            return naoAutenticado();
        }

        List<AdminAvaliacaoDTO> avaliacoes =
                avaliacaoService.listarTodas()
                        .stream()
                        .map(AdminAvaliacaoDTO::de)
                        .toList();

        return ResponseEntity.ok(avaliacoes);
    }

    @DeleteMapping("/avaliacoes/{avaliacaoId}")
    public ResponseEntity<?> excluirAvaliacao(
            @PathVariable Long avaliacaoId,
            HttpServletRequest request) {

        if (buscarUsuarioLogado(request) == null) {
            return naoAutenticado();
        }

        try {
            avaliacaoService.excluir(avaliacaoId);

            return ResponseEntity.ok(Map.of(
                    "mensagem",
                    "Avaliação removida com sucesso."
            ));
        } catch (IllegalArgumentException erro) {
            return ResponseEntity.badRequest().body(Map.of(
                    "erro",
                    erro.getMessage()
            ));
        }
    }

    private Usuario buscarUsuarioLogado(
            HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session == null) {
            return null;
        }

        Usuario usuarioSessao = (Usuario) session.getAttribute(
                "usuarioLogado"
        );

        if (usuarioSessao == null) {
            return null;
        }

        Usuario usuario = usuarioService.buscarPorId(
                usuarioSessao.getId()
        );

        if (usuario == null) {
            session.invalidate();
        }

        return usuario;
    }

    private ResponseEntity<?> naoAutenticado() {
        return ResponseEntity.status(401).body(Map.of(
                "erro",
                "Usuário não autenticado."
        ));
    }
}