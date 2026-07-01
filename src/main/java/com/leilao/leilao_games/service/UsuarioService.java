package com.leilao.leilao_games.service;

import com.leilao.leilao_games.model.Usuario;
import com.leilao.leilao_games.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public Usuario salvarUsuario(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public Usuario buscarPorId(Long id) {

        return usuarioRepository
                .findById(id)
                .orElse(null);

    }

    public long contarUsuarios() {
        return usuarioRepository.count();
    }

    public List<Usuario> listarTodos() {

        return usuarioRepository.findAll();

    }

    public void excluir(Long id) {

        usuarioRepository.deleteById(id);

    }

}