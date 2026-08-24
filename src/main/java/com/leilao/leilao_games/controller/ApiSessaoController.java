package com.leilao.leilao_games.controller;

import com.leilao.leilao_games.dto.LoginRequestDTO;
import com.leilao.leilao_games.dto.UsuarioDTO;
import com.leilao.leilao_games.model.Usuario;
import com.leilao.leilao_games.service.UsuarioService;
import com.leilao.leilao_games.dto.ProdutoResumoDTO;
import com.leilao.leilao_games.service.ProdutoService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.List;

@CrossOrigin(
        origins = "http://localhost:5173",
        allowCredentials = "true"
)
@RestController
@RequestMapping("/api/sessao")
@RequiredArgsConstructor
public class ApiSessaoController {

    private final UsuarioService usuarioService;

    private final ProdutoService produtoService;

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    @GetMapping
    public ResponseEntity<?> buscarUsuarioLogado(
            HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session == null) {
            return ResponseEntity
                    .status(401)
                    .body(Map.of(
                            "erro",
                            "Usuário não autenticado."
                    ));
        }

        Usuario usuarioSessao = (Usuario) session.getAttribute(
                "usuarioLogado"
        );

        if (usuarioSessao == null) {
            return ResponseEntity
                    .status(401)
                    .body(Map.of(
                            "erro",
                            "Usuário não autenticado."
                    ));
        }

        Usuario usuario = usuarioService.buscarPorId(
                usuarioSessao.getId()
        );

        if (usuario == null) {
            session.invalidate();

            return ResponseEntity
                    .status(401)
                    .body(Map.of(
                            "erro",
                            "Sessão inválida."
                    ));
        }

        return ResponseEntity.ok(
                UsuarioDTO.de(usuario)
        );
    }

         @GetMapping("/produtos")
        public ResponseEntity<?> listarMeusProdutos(
        HttpServletRequest request) {

    HttpSession session = request.getSession(false);

    if (session == null) {
        return ResponseEntity
                .status(401)
                .body(Map.of(
                        "erro",
                        "Usuário não autenticado."
                ));
    }

    Usuario usuarioSessao = (Usuario) session.getAttribute(
            "usuarioLogado"
    );

    if (usuarioSessao == null) {
        return ResponseEntity
                .status(401)
                .body(Map.of(
                        "erro",
                        "Usuário não autenticado."
                ));
    }

    Usuario usuario = usuarioService.buscarPorId(
            usuarioSessao.getId()
    );

    if (usuario == null) {
        session.invalidate();

        return ResponseEntity
                .status(401)
                .body(Map.of(
                        "erro",
                        "Sessão inválida."
                ));
    }

    List<ProdutoResumoDTO> produtos = produtoService
            .buscarPorUsuario(usuario.getId())
            .stream()
            .map(ProdutoResumoDTO::de)
            .toList();

    return ResponseEntity.ok(produtos);
}
    
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequestDTO dados,
            HttpServletRequest request,
            HttpSession session) {

        if (dados == null
                || dados.email() == null
                || dados.email().isBlank()
                || dados.senha() == null
                || dados.senha().isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "erro",
                            "E-mail e senha são obrigatórios."
                    ));
        }

        Usuario usuario = usuarioService.buscarPorEmail(
                dados.email().trim().toLowerCase()
        );

        if (usuario == null
                || !senhaCorreta(usuario, dados.senha())) {

            return ResponseEntity
                    .status(401)
                    .body(Map.of(
                            "erro",
                            "E-mail ou senha inválidos."
                    ));
        }

        request.changeSessionId();

        session.setAttribute(
                "usuarioLogado",
                usuario
        );

        session.setMaxInactiveInterval(30 * 60);

        return ResponseEntity.ok(
                UsuarioDTO.de(usuario)
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        return ResponseEntity.noContent().build();
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
}