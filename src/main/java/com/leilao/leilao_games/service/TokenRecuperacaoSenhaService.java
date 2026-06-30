package com.leilao.leilao_games.service;

import com.leilao.leilao_games.model.TokenRecuperacaoSenha;
import com.leilao.leilao_games.model.Usuario;
import com.leilao.leilao_games.repository.TokenRecuperacaoSenhaRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;

@Service
public class TokenRecuperacaoSenhaService {

    private final TokenRecuperacaoSenhaRepository repository;

    private final SecureRandom secureRandom =
            new SecureRandom();

    public TokenRecuperacaoSenhaService(
            TokenRecuperacaoSenhaRepository repository) {

        this.repository = repository;
    }

    @Transactional
    public String criar(Usuario usuario) {

        List<TokenRecuperacaoSenha> anteriores =
                repository
                        .findByUsuarioIdAndUtilizadoFalse(
                                usuario.getId()
                        );

        for (TokenRecuperacaoSenha anterior
                : anteriores) {

            anterior.setUtilizado(true);
        }

        repository.saveAll(anteriores);

        byte[] bytes = new byte[32];

        secureRandom.nextBytes(bytes);

        String token =
                Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(bytes);

        TokenRecuperacaoSenha recuperacao =
                new TokenRecuperacaoSenha();

        recuperacao.setUsuario(usuario);

        recuperacao.setTokenHash(
                gerarHash(token)
        );

        recuperacao.setExpiracao(
                LocalDateTime.now()
                        .plusMinutes(30)
        );

        recuperacao.setUtilizado(false);

        repository.save(recuperacao);

        return token;
    }

    @Transactional
    public TokenRecuperacaoSenha validar(
            String token) {

        if (token == null || token.isBlank()) {
            return null;
        }

        TokenRecuperacaoSenha recuperacao =
                repository
                        .findByTokenHashAndUtilizadoFalse(
                                gerarHash(token)
                        )
                        .orElse(null);

        if (recuperacao == null) {
            return null;
        }

        if (recuperacao.getExpiracao() == null
                || !LocalDateTime.now().isBefore(
                        recuperacao.getExpiracao()
                )) {

            recuperacao.setUtilizado(true);

            repository.save(recuperacao);

            return null;
        }

        return recuperacao;
    }

    @Transactional
    public void consumir(
            TokenRecuperacaoSenha recuperacao) {

        recuperacao.setUtilizado(true);

        repository.save(recuperacao);
    }

    private String gerarHash(String token) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );

            byte[] hash =
                    digest.digest(
                            token.getBytes(
                                    java.nio.charset.StandardCharsets.UTF_8
                            )
                    );

            return HexFormat.of()
                    .formatHex(hash);

        } catch (NoSuchAlgorithmException erro) {

            throw new IllegalStateException();
        }
    }
}