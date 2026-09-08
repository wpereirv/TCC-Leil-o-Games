package com.leilao.leilao_games.controller;

import com.leilao.leilao_games.dto.ProdutoDetalheDTO;
import com.leilao.leilao_games.model.Categoria;
import com.leilao.leilao_games.model.Lance;
import com.leilao.leilao_games.model.Produto;
import com.leilao.leilao_games.model.Usuario;
import com.leilao.leilao_games.service.CategoriaService;
import com.leilao.leilao_games.service.ImagemService;
import com.leilao.leilao_games.service.LanceService;
import com.leilao.leilao_games.service.NotificacaoService;
import com.leilao.leilao_games.service.ProdutoService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@CrossOrigin(
        origins = "${app.cors.allowed-origin}",
        allowCredentials = "true"
)
@RestController
@RequestMapping("/api/anuncios")
@RequiredArgsConstructor
public class ApiEdicaoAnuncioController {

    private final ProdutoService produtoService;
    private final CategoriaService categoriaService;
    private final LanceService lanceService;
    private final ImagemService imagemService;
    private final NotificacaoService notificacaoService;

    @GetMapping("/{produtoId}/edicao")
    public ResponseEntity<?> buscarParaEdicao(
            @PathVariable Long produtoId,
            HttpServletRequest request) {

        Usuario usuario = buscarUsuarioLogado(request);

        if (usuario == null) {
            return naoAutenticado();
        }

        Produto produto = produtoService.buscarPorId(produtoId);

        if (produto == null) {
            return ResponseEntity.notFound().build();
        }

        if (produto.getUsuario() == null
                || !produto.getUsuario().getId()
                        .equals(usuario.getId())) {

            return ResponseEntity.status(403).body(Map.of(
                    "erro",
                    "Você não pode editar este anúncio."
            ));
        }

        if (Boolean.TRUE.equals(produto.getEncerrado())) {
            return ResponseEntity.status(409).body(Map.of(
                    "erro",
                    "Leilões encerrados não podem ser editados."
            ));
        }

        boolean possuiLances = !lanceService
                .buscarPorProduto(produtoId)
                .isEmpty();

        return ResponseEntity.ok(Map.of(
                "produto",
                ProdutoDetalheDTO.de(
                        produto,
                        lanceService.buscarMaiorLance(produtoId)
                ),
                "possuiLances",
                possuiLances
        ));
    }

    @PutMapping(
            value = "/{produtoId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> editar(
            @PathVariable Long produtoId,

            @RequestParam String nome,
            @RequestParam String descricao,
            @RequestParam Long categoriaId,
            @RequestParam BigDecimal valorInicial,

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

            HttpServletRequest request) {

        Usuario usuario = buscarUsuarioLogado(request);

        if (usuario == null) {
            return naoAutenticado();
        }

        Produto produto = produtoService.buscarPorId(produtoId);

        if (produto == null) {
            return ResponseEntity.status(404).body(Map.of(
                    "erro",
                    "Produto não encontrado."
            ));
        }

        if (produto.getUsuario() == null
                || !produto.getUsuario().getId()
                        .equals(usuario.getId())) {

            return ResponseEntity.status(403).body(Map.of(
                    "erro",
                    "Você não pode editar este anúncio."
            ));
        }

        if (Boolean.TRUE.equals(produto.getEncerrado())) {
            return ResponseEntity.status(409).body(Map.of(
                    "erro",
                    "Leilões encerrados não podem ser editados."
            ));
        }

        List<Lance> lances =
                lanceService.buscarPorProduto(produtoId);

        boolean possuiLances = !lances.isEmpty();

        String nomeEfetivo = possuiLances
                ? produto.getNome()
                : nome;

        Long categoriaEfetivaId = possuiLances
                ? produto.getCategoria().getId()
                : categoriaId;

        BigDecimal valorEfetivo = possuiLances
                ? produto.getValorInicial()
                : valorInicial;

        if (nomeEfetivo == null
                || nomeEfetivo.isBlank()
                || nomeEfetivo.trim().length() > 120
                || descricao == null
                || descricao.isBlank()
                || descricao.trim().length() > 2000
                || categoriaEfetivaId == null
                || valorEfetivo == null
                || valorEfetivo.compareTo(BigDecimal.ZERO) <= 0
                || valorEfetivo.scale() > 2) {

            return ResponseEntity.badRequest().body(Map.of(
                    "erro",
                    "Preencha os campos obrigatórios corretamente."
            ));
        }

        Categoria categoria =
                categoriaService.buscarPorId(categoriaEfetivaId);

        if (categoria == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "erro",
                    "A categoria informada não existe."
            ));
        }

        String imagemAntiga1 = produto.getImagem1();
        String imagemAntiga2 = produto.getImagem2();
        String imagemAntiga3 = produto.getImagem3();

        try {
            imagemService.validar(foto1);
            imagemService.validar(foto2);
            imagemService.validar(foto3);

            String imagemNova1 = imagemService.salvar(foto1);
            String imagemNova2 = imagemService.salvar(foto2);
            String imagemNova3 = imagemService.salvar(foto3);

            boolean descricaoAlterada = !Objects.equals(
                    produto.getDescricao(),
                    descricao.trim()
            );

            boolean imagensAlteradas =
                    imagemNova1 != null
                    || imagemNova2 != null
                    || imagemNova3 != null
                    || removerImagem1
                    || removerImagem2
                    || removerImagem3;

            if (imagemNova1 != null) {
                produto.setImagem1(imagemNova1);
            } else if (removerImagem1) {
                produto.setImagem1(null);
            }

            if (imagemNova2 != null) {
                produto.setImagem2(imagemNova2);
            } else if (removerImagem2) {
                produto.setImagem2(null);
            }

            if (imagemNova3 != null) {
                produto.setImagem3(imagemNova3);
            } else if (removerImagem3) {
                produto.setImagem3(null);
            }

            if (!possuiLances) {
                produto.setNome(nomeEfetivo.trim());
                produto.setCategoria(categoria);
                produto.setValorInicial(valorEfetivo);
            }

            produto.setDescricao(descricao.trim());

            produtoService.salvar(produto);

            removerImagemAntiga(
                    imagemNova1,
                    removerImagem1,
                    imagemAntiga1
            );

            removerImagemAntiga(
                    imagemNova2,
                    removerImagem2,
                    imagemAntiga2
            );

            removerImagemAntiga(
                    imagemNova3,
                    removerImagem3,
                    imagemAntiga3
            );

            if (possuiLances
                    && (descricaoAlterada || imagensAlteradas)) {

                notificarParticipantes(
                        produto,
                        lances,
                        descricaoAlterada,
                        imagensAlteradas
                );
            }

            return ResponseEntity.ok(
                    ProdutoDetalheDTO.de(
                            produto,
                            lanceService.buscarMaiorLance(
                                    produto.getId()
                            )
                    )
            );

        } catch (IOException
                | IllegalArgumentException erro) {

            return ResponseEntity.badRequest().body(Map.of(
                    "erro",
                    "Envie apenas imagens JPG ou PNG de até 5 MB."
            ));
        }
    }

    private void removerImagemAntiga(
            String imagemNova,
            boolean removerImagem,
            String imagemAntiga) throws IOException {

        if ((imagemNova != null || removerImagem)
                && imagemAntiga != null) {

            imagemService.remover(imagemAntiga);
        }
    }

    private void notificarParticipantes(
            Produto produto,
            List<Lance> lances,
            boolean descricaoAlterada,
            boolean imagensAlteradas) {

        String alteracao;

        if (descricaoAlterada && imagensAlteradas) {
            alteracao = "a descrição e as imagens";
        } else if (descricaoAlterada) {
            alteracao = "a descrição";
        } else {
            alteracao = "as imagens";
        }

        Set<Long> usuariosNotificados = new HashSet<>();

        for (Lance lance : lances) {
            Usuario participante = lance.getUsuario();

            if (participante != null
                    && participante.getId() != null
                    && usuariosNotificados.add(
                            participante.getId()
                    )) {

                notificacaoService.criar(
                        participante,
                        "ANUNCIO_ATUALIZADO",
                        "O vendedor atualizou "
                                + alteracao
                                + " do anúncio \""
                                + produto.getNome()
                                + "\". Consulte os detalhes.",
                        "/produto/" + produto.getId()
                );
            }
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