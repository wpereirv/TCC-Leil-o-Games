package com.leilao.leilao_games.controller;

import com.leilao.leilao_games.dto.LoginRequestDTO;
import com.leilao.leilao_games.dto.CadastroRequestDTO;
import com.leilao.leilao_games.dto.UsuarioDTO;
import com.leilao.leilao_games.model.Usuario;
import com.leilao.leilao_games.service.UsuarioService;
import com.leilao.leilao_games.dto.ProdutoResumoDTO;
import com.leilao.leilao_games.service.ProdutoService;
import com.leilao.leilao_games.dto.ResumoContaDTO;
import com.leilao.leilao_games.service.LanceService;
import com.leilao.leilao_games.service.FavoritoService;

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
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Map;
import java.util.List;

@CrossOrigin(
        origins = "${app.cors.allowed-origin}",
        allowCredentials = "true"
)
@RestController
@RequestMapping("/api/sessao")
@RequiredArgsConstructor
public class ApiSessaoController {

    private final UsuarioService usuarioService;
    private final ProdutoService produtoService;
    private final LanceService lanceService;
    private final FavoritoService favoritoService;

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

        @GetMapping("/resumo")
public ResponseEntity<?> buscarResumo(
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

    ResumoContaDTO resumo = new ResumoContaDTO(
            UsuarioDTO.de(usuario),
            produtoService.contarProdutosUsuario(
                    usuario.getId()
            ),
            lanceService.contarLancesUsuario(
                    usuario.getId()
            ),
            favoritoService.contarFavoritosUsuario(
                    usuario.getId()
            )
    );

    return ResponseEntity.ok(resumo);
}

        @PostMapping("/cadastro")
    public ResponseEntity<?> cadastrar(
            @RequestBody CadastroRequestDTO dados) {

        if (dados == null
                || dados.nome() == null
                || dados.nome().isBlank()
                || dados.email() == null
                || dados.email().isBlank()
                || dados.senha() == null
                || dados.senha().isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "erro",
                            "Preencha todos os campos."
                    ));
        }

        String nome = dados.nome().trim();
        String email = dados.email().trim().toLowerCase();

        if (nome.length() > 120
                || email.length() > 180) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "erro",
                            "Dados inválidos."
                    ));
        }

        if (dados.senha().length() < 6) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "erro",
                            "A senha deve possuir pelo menos 6 caracteres."
                    ));
        }

        if (usuarioService.buscarPorEmail(email) != null) {
            return ResponseEntity
                    .status(409)
                    .body(Map.of(
                            "erro",
                            "Este e-mail já está cadastrado."
                    ));
        }

        Usuario usuario = new Usuario();

        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setTipo("CLIENTE");
        usuario.setSenha(
                passwordEncoder.encode(dados.senha())
        );

        try {
            usuarioService.salvarUsuario(usuario);
        } catch (DataIntegrityViolationException erro) {
            return ResponseEntity
                    .status(409)
                    .body(Map.of(
                            "erro",
                            "Este e-mail já está cadastrado."
                    ));
        }

        return ResponseEntity
                .status(201)
                .body(UsuarioDTO.de(usuario));
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

        if (!Boolean.TRUE.equals(usuario.getAtivo())) {
                return ResponseEntity
                        .status(403)
                        .body(Map.of(
                                "erro",
                                "Esta conta está desativada. Entre em contato com a administração."
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