package com.leilao.leilao_games.controller;

import com.leilao.leilao_games.dto.PerfilRequestDTO;
import com.leilao.leilao_games.dto.UsuarioDTO;
import com.leilao.leilao_games.model.Usuario;
import com.leilao.leilao_games.service.UsuarioService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@CrossOrigin(
        origins = "${app.cors.allowed-origin}",
        allowCredentials = "true"
)
@RestController
@RequestMapping("/api/perfil")
@RequiredArgsConstructor
public class ApiPerfilController {

    private final UsuarioService usuarioService;

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    @PutMapping
    public ResponseEntity<?> atualizar(
            @RequestBody PerfilRequestDTO dados,
            HttpServletRequest request) {

        Usuario usuarioSessao = buscarUsuarioLogado(request);

        if (usuarioSessao == null) {
            return naoAutenticado();
        }

        Usuario usuario = usuarioService.buscarPorId(
                usuarioSessao.getId()
        );

        if (usuario == null) {
            HttpSession session = request.getSession(false);

            if (session != null) {
                session.invalidate();
            }

            return naoAutenticado();
        }

        if (dados == null
                || dados.nome() == null
                || dados.nome().isBlank()
                || dados.nome().trim().length() > 120
                || dados.email() == null
                || dados.email().isBlank()
                || dados.email().trim().length() > 180
                || !dados.email().trim().matches(
                        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
                )) {

            return ResponseEntity.badRequest().body(Map.of(
                    "erro",
                    "Informe nome e e-mail válidos."
            ));
        }

        if (dados.senhaAtual() == null
                || !senhaCorreta(usuario, dados.senhaAtual())) {

            return ResponseEntity.status(401).body(Map.of(
                    "erro",
                    "A senha atual está incorreta."
            ));
        }

        String email = dados.email().trim().toLowerCase();

        Usuario usuarioMesmoEmail =
                usuarioService.buscarPorEmail(email);

        if (usuarioMesmoEmail != null
                && !usuarioMesmoEmail.getId()
                        .equals(usuario.getId())) {

            return ResponseEntity.status(409).body(Map.of(
                    "erro",
                    "Este e-mail já está cadastrado."
            ));
        }

        if (dados.novaSenha() != null
                && !dados.novaSenha().isBlank()) {

            if (dados.novaSenha().length() < 6
                    || dados.novaSenha().length() > 72
                    || dados.confirmarSenha() == null
                    || !dados.novaSenha().equals(
                            dados.confirmarSenha()
                    )) {

                return ResponseEntity.badRequest().body(Map.of(
                        "erro",
                        "A nova senha deve ter ao menos 6 caracteres e ser confirmada corretamente."
                ));
            }

            usuario.setSenha(
                    passwordEncoder.encode(dados.novaSenha())
            );
        }

        usuario.setNome(dados.nome().trim());
        usuario.setEmail(email);

        try {
            usuarioService.salvarUsuario(usuario);
        } catch (DataIntegrityViolationException erro) {
            return ResponseEntity.status(409).body(Map.of(
                    "erro",
                    "Este e-mail já está cadastrado."
            ));
        }

        HttpSession session = request.getSession(false);

        if (session != null) {
            session.setAttribute("usuarioLogado", usuario);
        }

        return ResponseEntity.ok(UsuarioDTO.de(usuario));
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

    private boolean senhaCorreta(
            Usuario usuario,
            String senhaDigitada) {

        String senhaArmazenada = usuario.getSenha();

        if (senhaArmazenada == null) {
            return false;
        }

        if (senhaArmazenada.startsWith("$2a$")
                || senhaArmazenada.startsWith("$2b$")
                || senhaArmazenada.startsWith("$2y$")) {

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

    private ResponseEntity<?> naoAutenticado() {
        return ResponseEntity.status(401).body(Map.of(
                "erro",
                "Usuário não autenticado."
        ));
    }
}