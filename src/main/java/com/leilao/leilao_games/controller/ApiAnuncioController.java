package com.leilao.leilao_games.controller;

import com.leilao.leilao_games.dto.ProdutoResumoDTO;
import com.leilao.leilao_games.model.Categoria;
import com.leilao.leilao_games.model.Produto;
import com.leilao.leilao_games.model.Usuario;
import com.leilao.leilao_games.service.CategoriaService;
import com.leilao.leilao_games.service.ImagemService;
import com.leilao.leilao_games.service.ProdutoService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@CrossOrigin(
        origins = "http://localhost:5173",
        allowCredentials = "true"
)
@RestController
@RequestMapping("/api/anuncios")
@RequiredArgsConstructor
public class ApiAnuncioController {

    private final ProdutoService produtoService;
    private final CategoriaService categoriaService;
    private final ImagemService imagemService;

    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> criarAnuncio(
            @RequestParam String nome,
            @RequestParam String descricao,
            @RequestParam Long categoriaId,
            @RequestParam BigDecimal valorInicial,
            @RequestParam Integer diasLeilao,

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

            HttpServletRequest request) {

        Usuario usuario = buscarUsuarioLogado(request);

        if (usuario == null) {
            return naoAutenticado();
        }

        if (nome == null
                || nome.isBlank()
                || nome.trim().length() > 120
                || descricao == null
                || descricao.isBlank()
                || descricao.trim().length() > 2000
                || categoriaId == null
                || valorInicial == null
                || valorInicial.compareTo(BigDecimal.ZERO) <= 0
                || valorInicial.scale() > 2) {

            return ResponseEntity.badRequest().body(Map.of(
                    "erro",
                    "Preencha os campos obrigatórios corretamente."
            ));
        }

        Categoria categoria =
                categoriaService.buscarPorId(categoriaId);

        if (categoria == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "erro",
                    "A categoria informada não existe."
            ));
        }

        if (diasLeilao == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "erro",
                    "Informe a duração do leilão."
            ));
        }

        int duracao = Math.max(1, Math.min(5, diasLeilao));

        try {
            imagemService.validar(foto1);
            imagemService.validar(foto2);
            imagemService.validar(foto3);

            Produto produto = new Produto();

            produto.setNome(nome.trim());
            produto.setDescricao(descricao.trim());
            produto.setCategoria(categoria);
            produto.setValorInicial(valorInicial);
            produto.setImagem1(imagemService.salvar(foto1));
            produto.setImagem2(imagemService.salvar(foto2));
            produto.setImagem3(imagemService.salvar(foto3));
            produto.setUsuario(usuario);
            produto.setDataInicio(LocalDateTime.now());
            produto.setDataFim(
                    LocalDateTime.now().plusDays(duracao)
            );
            produto.setEncerrado(false);
            produto.setAvaliado(false);

            Produto produtoSalvo =
                    produtoService.salvar(produto);

            return ResponseEntity.status(201).body(
                    ProdutoResumoDTO.de(produtoSalvo)
            );

        } catch (IOException
                | IllegalArgumentException erro) {

            return ResponseEntity.badRequest().body(Map.of(
                    "erro",
                    "Envie apenas imagens JPG ou PNG de até 5 MB."
            ));
        }
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

    private ResponseEntity<?> naoAutenticado() {
        return ResponseEntity.status(401).body(Map.of(
                "erro",
                "Usuário não autenticado."
        ));
    }
}