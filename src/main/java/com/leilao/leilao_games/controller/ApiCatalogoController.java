package com.leilao.leilao_games.controller;

import com.leilao.leilao_games.dto.CategoriaDTO;
import com.leilao.leilao_games.dto.ProdutoResumoDTO;
import com.leilao.leilao_games.service.CategoriaService;
import com.leilao.leilao_games.service.ProdutoService;
import com.leilao.leilao_games.dto.ProdutoDetalheDTO;
import com.leilao.leilao_games.model.Produto;
import com.leilao.leilao_games.service.LanceService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiCatalogoController {

    private final ProdutoService produtoService;
    private final CategoriaService categoriaService;
    private final LanceService lanceService;

    @GetMapping("/produtos")
    public List<ProdutoResumoDTO> listarProdutos() {

        return produtoService
                .listarAtivos()
                .stream()
                .map(ProdutoResumoDTO::de)
                .toList();
    }

    @GetMapping("/categorias")
    public List<CategoriaDTO> listarCategorias() {

        return categoriaService
                .listarTodas()
                .stream()
                .map(CategoriaDTO::de)
                .toList();
    }

    @GetMapping("/produtos/{id}")
    public ResponseEntity<ProdutoDetalheDTO> buscarProduto(
        @PathVariable Long id) {

    Produto produto =
            produtoService.buscarPorId(id);

    if (produto == null) {
        return ResponseEntity
                .notFound()
                .build();
    }

    return ResponseEntity.ok(
            ProdutoDetalheDTO.de(
                    produto,
                    lanceService.buscarMaiorLance(id)
            )
    );
}
}