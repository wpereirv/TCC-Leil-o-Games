package com.leilao.leilao_games.controller;

import com.leilao.leilao_games.model.Usuario;
import com.leilao.leilao_games.service.UsuarioService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();
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

        usuario.setSenha(
                passwordEncoder.encode(usuario.getSenha())
        );

        try {

    usuarioService.salvarUsuario(usuario);

} catch (DataIntegrityViolationException erro) {

    return "redirect:/cadastro?erro=emailExistente";
}

return "redirect:/login?cadastro=sucesso";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String email,
            @RequestParam String senha,
            HttpSession session,
            HttpServletRequest request) {

        if (email == null
                || email.isBlank()
                || senha == null
                || senha.isBlank()) {

            return "redirect:/login?erro=credenciais";
        }

        Usuario usuario = usuarioService.buscarPorEmail(
                email.trim().toLowerCase()
        );

        if (usuario == null
                || !senhaCorreta(usuario, senha)) {

            return "redirect:/login?erro=credenciais";
        }

        request.changeSessionId();

        session.setAttribute("usuarioLogado", usuario);

        session.setMaxInactiveInterval(30 * 60);

        return "redirect:/";
    }

    private boolean senhaCorreta(
            Usuario usuario,
            String senhaDigitada) {

        String senhaArmazenada = usuario.getSenha();

        if (senhaArmazenada == null) {
            return false;
        }

        if (ehSenhaCriptografada(senhaArmazenada)) {
            return passwordEncoder.matches(
                    senhaDigitada,
                    senhaArmazenada
            );
        }

        if (senhaArmazenada.equals(senhaDigitada)) {

            usuario.setSenha(
                    passwordEncoder.encode(senhaDigitada)
            );

            usuarioService.salvarUsuario(usuario);

            return true;
        }

        return false;
    }

    private boolean ehSenhaCriptografada(String senha) {
        return senha.startsWith("$2a$")
                || senha.startsWith("$2b$")
                || senha.startsWith("$2y$");
    }

    @GetMapping("/perfil/editar")
public String abrirEdicaoPerfil(
        HttpSession session,
        Model model) {

    Usuario usuarioSessao =
            (Usuario) session.getAttribute(
                    "usuarioLogado"
            );

    if (usuarioSessao == null) {
        return "redirect:/login";
    }

    Usuario usuario =
            usuarioService.buscarPorId(
                    usuarioSessao.getId()
            );

    if (usuario == null) {
        session.invalidate();

        return "redirect:/login";
    }

    model.addAttribute("usuario", usuario);

    return "editar-perfil";
}

@PostMapping("/perfil/editar")
public String salvarEdicaoPerfil(
        @RequestParam String nome,
        @RequestParam String email,
        @RequestParam String senhaAtual,
        @RequestParam(
                required = false
        )
        String novaSenha,
        @RequestParam(
                required = false
        )
        String confirmarSenha,
        HttpSession session) {

    Usuario usuarioSessao =
            (Usuario) session.getAttribute(
                    "usuarioLogado"
            );

    if (usuarioSessao == null) {
        return "redirect:/login";
    }

    Usuario usuario =
            usuarioService.buscarPorId(
                    usuarioSessao.getId()
            );

    if (usuario == null) {
        session.invalidate();

        return "redirect:/login";
    }

    if (nome == null
            || nome.isBlank()
            || nome.length() > 120
            || email == null
            || email.isBlank()
            || email.length() > 180
            || !email.matches(
                    "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
            )) {

        return "redirect:/perfil/editar?erro=campos";
    }

    if (senhaAtual == null
            || !senhaCorreta(
                    usuario,
                    senhaAtual
            )) {

        return "redirect:/perfil/editar?erro=senhaAtual";
    }

    String emailNormalizado =
            email.trim().toLowerCase();

    Usuario usuarioMesmoEmail =
            usuarioService.buscarPorEmail(
                    emailNormalizado
            );

    if (usuarioMesmoEmail != null
            && !usuarioMesmoEmail.getId()
                    .equals(usuario.getId())) {

        return "redirect:/perfil/editar?erro=emailExistente";
    }

    if (novaSenha != null && !novaSenha.isBlank()) {

        if (novaSenha.length() < 6
                || novaSenha.length() > 72
                || confirmarSenha == null
                || !novaSenha.equals(
                        confirmarSenha
                )) {

            return "redirect:/perfil/editar?erro=senhas";
        }

        usuario.setSenha(
                passwordEncoder.encode(novaSenha)
        );
    }

    usuario.setNome(nome.trim());
    usuario.setEmail(emailNormalizado);

    try {

        usuarioService.salvarUsuario(usuario);

    } catch (DataIntegrityViolationException erro) {

        return "redirect:/perfil/editar?erro=emailExistente";
    }

    session.setAttribute(
            "usuarioLogado",
            usuario
    );

    return "redirect:/minha-conta?sucesso=perfil";
}

    @PostMapping("/logout")
    public String logout(HttpSession session) {

        session.invalidate();

        return "redirect:/login?logout=sucesso";
    }
}