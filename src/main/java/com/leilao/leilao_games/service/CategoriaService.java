package com.leilao.leilao_games.service;

import com.leilao.leilao_games.model.Categoria;
import com.leilao.leilao_games.repository.CategoriaRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    public Categoria salvar(Categoria categoria) {

        if (categoria == null
                || categoria.getNome() == null
                || categoria.getNome().isBlank()) {

            throw new IllegalArgumentException(
                    "O nome da categoria é obrigatório."
            );
        }

        String nome = categoria.getNome().trim();

        boolean nomeDuplicado;

        if (categoria.getId() == null) {

            nomeDuplicado =
                    categoriaRepository
                            .existsByNomeIgnoreCase(nome);

        } else {

            nomeDuplicado =
                    categoriaRepository
                            .existsByNomeIgnoreCaseAndIdNot(
                                    nome,
                                    categoria.getId()
                            );
        }

        if (nomeDuplicado) {

            throw new IllegalArgumentException(
                    "Já existe uma categoria com esse nome."
            );
        }

        categoria.setNome(nome);

        if (categoria.getDescricao() != null) {

            categoria.setDescricao(
                    categoria.getDescricao().trim()
            );
        }

        return categoriaRepository.save(categoria);
    }

    public List<Categoria> listarTodas() {

        return categoriaRepository.findAll();
    }

    public Categoria buscarPorId(Long id) {

        return categoriaRepository
                .findById(id)
                .orElse(null);
    }

    public void excluir(Long id) {

        categoriaRepository.deleteById(id);
    }

    public long contarCategorias() {

    return categoriaRepository.count();
    }
}