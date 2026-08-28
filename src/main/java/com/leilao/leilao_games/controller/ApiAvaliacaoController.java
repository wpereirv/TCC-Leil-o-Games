package com.leilao.leilao_games.controller;

import com.leilao.leilao_games.dto.AvaliacaoDTO;
import com.leilao.leilao_games.dto.AvaliacaoRequestDTO;
import com.leilao.leilao_games.model.Avaliacao;
import com.leilao.leilao_games.model.Lance;
import com.leilao.leilao_games.model.Produto;
import com.leilao.leilao_games.model.Usuario;
import com.leilao.leilao_games.service.AvaliacaoService;
import com.leilao.leilao_games.service.LanceService;
import com.leilao.leilao_games.service.ProdutoService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@CrossOrigin(
        origins = "http://localhost:5173",
        allowCredentials = "true"
)
@RestController
@RequestMapping("/api/produtos/{produtoId}/avaliacoes")
@RequiredArgsConstructor
public class ApiAvaliacaoController {

    private final AvaliacaoService avaliacaoService;
    private final ProdutoService produtoService;
    private final LanceService lanceService;

    @GetMapping
    public ResponseEntity<?> listar(
            @PathVariable Long produtoId) {

        Produto produto = produtoService.buscarPorId(produtoId);

        if (produto == null) {
            return ResponseEntity.notFound().build();
        }

        List<AvaliacaoDTO> avaliacoes =
                avaliacaoService.buscarPorVendedor(
                                produto.getUsuario().getId()
                        )
                        .stream()
                        .map(AvaliacaoDTO::de)
                        .toList();

        return ResponseEntity.ok(avaliacoes);
    }

    @PostMapping
    public ResponseEntity<?> avaliar(
            @PathVariable Long produtoId,
            @RequestBody AvaliacaoRequestDTO dados,
            HttpServletRequest request) {

        Usuario comprador = buscarUsuarioLogado(request);

        if (comprador == null) {
            return ResponseEntity
                    .status(401)
                    .body(Map.of(
                            "erro",
                            "Usuário não autenticado."
                    ));
        }

        if (dados.nota() == null
                || dados.nota() < 1
                || dados.nota() > 5
                || dados.comentario() == null
                || dados.comentario().isBlank()
                || dados.comentario().trim().length() > 1000) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "erro",
                            "Informe uma nota de 1 a 5 e um comentário de até 1000 caracteres."
                    ));
        }

        Produto produto = produtoService.buscarPorId(produtoId);

        if (produto == null) {
            return ResponseEntity
                    .status(404)
                    .body(Map.of(
                            "erro",
                            "Produto não encontrado."
                    ));
        }

        if (!Boolean.TRUE.equals(produto.getEncerrado())) {
            return ResponseEntity
                    .status(403)
                    .body(Map.of(
                            "erro",
                            "O leilão ainda não foi encerrado."
                    ));
        }

        Lance vencedor = lanceService.buscarLanceVencedor(produtoId);

        if (vencedor == null
                || !vencedor.getUsuario().getId()
                .equals(comprador.getId())) {

            return ResponseEntity
                    .status(403)
                    .body(Map.of(
                            "erro",
                            "Somente o vencedor pode avaliar o vendedor."
                    ));
        }

        if (produto.getUsuario().getId()
                .equals(comprador.getId())) {

            return ResponseEntity
                    .status(403)
                    .body(Map.of(
                            "erro",
                            "O vendedor não pode avaliar o próprio anúncio."
                    ));
        }

        if (avaliacaoService.produtoJaAvaliado(produtoId)) {
            return ResponseEntity
                    .status(409)
                    .body(Map.of(
                            "erro",
                            "Este produto já foi avaliado."
                    ));
        }

        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setProduto(produto);
        avaliacao.setComprador(comprador);
        avaliacao.setVendedor(produto.getUsuario());
        avaliacao.setNota(dados.nota());
        avaliacao.setComentario(dados.comentario().trim());

        try {
            avaliacaoService.salvar(avaliacao);

            produto.setAvaliado(true);
            produtoService.salvar(produto);
        } catch (DataIntegrityViolationException
                | IllegalArgumentException erro) {

            return ResponseEntity
                    .status(409)
                    .body(Map.of(
                            "erro",
                            "Este produto já foi avaliado."
                    ));
        }

        return ResponseEntity
                .status(201)
                .body(AvaliacaoDTO.de(avaliacao));
    }

    private Usuario buscarUsuarioLogado(
            HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session == null) {
            return null;
        }

        return (Usuario) session.getAttribute(
                "usuarioLogado"
        );
    }
}