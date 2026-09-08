package com.leilao.leilao_games.controller;

import com.leilao.leilao_games.dto.ProdutoResumoDTO;
import com.leilao.leilao_games.model.Favorito;
import com.leilao.leilao_games.model.Produto;
import com.leilao.leilao_games.model.Usuario;
import com.leilao.leilao_games.service.FavoritoService;
import com.leilao.leilao_games.service.ProdutoService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@CrossOrigin(
        origins = "${app.cors.allowed-origin}",
        allowCredentials = "true"
)
@RestController
@RequestMapping("/api/favoritos")
@RequiredArgsConstructor
public class ApiFavoritoController {

    private final FavoritoService favoritoService;
    private final ProdutoService produtoService;

    @GetMapping
    public ResponseEntity<?> listarFavoritos(HttpServletRequest request) {

        Usuario usuario = buscarUsuarioLogado(request);

        if (usuario == null) {
            return naoAutenticado();
        }

        List<ProdutoResumoDTO> favoritos = favoritoService
                .buscarPorUsuario(usuario.getId())
                .stream()
                .filter(favorito -> favorito.getProduto() != null)
                .map(favorito -> ProdutoResumoDTO.de(favorito.getProduto()))
                .toList();

        return ResponseEntity.ok(favoritos);
    }

    @GetMapping("/{produtoId}")
    public ResponseEntity<?> verificarFavorito(
            @PathVariable Long produtoId,
            HttpServletRequest request) {

        Usuario usuario = buscarUsuarioLogado(request);

        if (usuario == null) {
            return naoAutenticado();
        }

        boolean favorito = favoritoService.jaExiste(
                usuario.getId(),
                produtoId
        );

        return ResponseEntity.ok(Map.of(
                "favorito", favorito
        ));
    }

    @PostMapping("/{produtoId}")
    public ResponseEntity<?> alternarFavorito(
            @PathVariable Long produtoId,
            HttpServletRequest request) {

        Usuario usuario = buscarUsuarioLogado(request);

        if (usuario == null) {
            return naoAutenticado();
        }

        boolean jaFavoritado = favoritoService.jaExiste(
                usuario.getId(),
                produtoId
        );

        if (jaFavoritado) {
            favoritoService.remover(usuario.getId(), produtoId);

            return ResponseEntity.ok(Map.of(
                    "favorito", false,
                    "mensagem", "Produto removido dos favoritos."
            ));
        }

        Produto produto = produtoService.buscarPorId(produtoId);

        if (produto == null) {
            return ResponseEntity.status(404).body(Map.of(
                    "erro", "Produto não encontrado."
            ));
        }

        if (produto.getUsuario() != null
                && produto.getUsuario().getId().equals(usuario.getId())) {

            return ResponseEntity.status(403).body(Map.of(
                    "erro", "Você não pode favoritar o seu próprio anúncio."
            ));
        }

        Favorito favorito = new Favorito();
        favorito.setUsuario(usuario);
        favorito.setProduto(produto);

        favoritoService.salvar(favorito);

        return ResponseEntity.ok(Map.of(
                "favorito", true,
                "mensagem", "Produto adicionado aos favoritos."
        ));
    }

    private Usuario buscarUsuarioLogado(HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session == null) {
            return null;
        }

        return (Usuario) session.getAttribute("usuarioLogado");
    }

    private ResponseEntity<?> naoAutenticado() {
        return ResponseEntity.status(401).body(Map.of(
                "erro", "Usuário não autenticado."
        ));
    }
}