package com.leilao.leilao_games.controller;

import com.leilao.leilao_games.model.Usuario;
import com.leilao.leilao_games.service.UsuarioService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

@Controller
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/cadastro")
public String cadastrar(Usuario usuario) {

    if (usuario.getNome() == null
            || usuario.getNome().isBlank()
            || usuario.getEmail() == null
            || usuario.getEmail().isBlank()
            || usuario.getSenha() == null
            || usuario.getSenha().isBlank()) {

        return "redirect:/cadastro?erro=campos";
    }

    usuario.setNome(usuario.getNome().trim());

    usuario.setEmail(
            usuario.getEmail()
                    .trim()
                    .toLowerCase()
    );

    if (usuario.getSenha().length() < 6) {

        return "redirect:/cadastro?erro=senhaCurta";
    }

    if (usuarioService.buscarPorEmail(
            usuario.getEmail()) != null) {

        return "redirect:/cadastro?erro=emailExistente";
    }

    usuario.setTipo("CLIENTE");

    usuarioService.salvarUsuario(usuario);

    return "redirect:/login?cadastro=sucesso";
}
    
    @PostMapping("/login")
public String login(
        @RequestParam String email,
        @RequestParam String senha,
        HttpSession session) {

    Usuario usuario = usuarioService.buscarPorEmail(email);

    if (usuario != null &&
        usuario.getSenha().equals(senha)) {

        session.setAttribute("usuarioLogado", usuario);

        return "redirect:/";
    }

    return "redirect:/login?erro=credenciais";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {

    session.invalidate();

    return "redirect:/login?logout=sucesso";
}
}