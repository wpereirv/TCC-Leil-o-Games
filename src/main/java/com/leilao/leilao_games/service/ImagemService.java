package com.leilao.leilao_games.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class ImagemService {

    private static final long TAMANHO_MAXIMO =
            5L * 1024 * 1024;

    private static final int DIMENSAO_MAXIMA = 6000;

    private static final Set<String> TIPOS_PERMITIDOS =
            Set.of(
                    "image/jpeg",
                    "image/png"
            );

    private final Path diretorioUploads =
            Paths.get("src/main/resources/static/uploads")
                    .toAbsolutePath()
                    .normalize();

    public void validar(MultipartFile arquivo)
            throws IOException {

        if (arquivo == null || arquivo.isEmpty()) {
            return;
        }

        if (arquivo.getSize() > TAMANHO_MAXIMO) {
            throw new IllegalArgumentException();
        }

        String tipo = arquivo.getContentType();

        if (tipo == null
                || !TIPOS_PERMITIDOS.contains(tipo)) {

            throw new IllegalArgumentException();
        }

        BufferedImage imagem;

        try (InputStream entrada =
                     arquivo.getInputStream()) {

            imagem = ImageIO.read(entrada);
        }

        if (imagem == null) {
            throw new IllegalArgumentException();
        }

        if (imagem.getWidth() > DIMENSAO_MAXIMA
                || imagem.getHeight() > DIMENSAO_MAXIMA) {

            throw new IllegalArgumentException();
        }
    }

    public String salvar(MultipartFile arquivo)
            throws IOException {

        if (arquivo == null || arquivo.isEmpty()) {
            return null;
        }

        validar(arquivo);

        Files.createDirectories(diretorioUploads);

        String extensao =
                "image/png".equals(arquivo.getContentType())
                        ? ".png"
                        : ".jpg";

        String nomeSeguro =
                UUID.randomUUID() + extensao;

        Path destino =
                diretorioUploads.resolve(nomeSeguro)
                        .normalize();

        if (!destino.startsWith(diretorioUploads)) {
            throw new IllegalArgumentException();
        }

        try (InputStream entrada =
                     arquivo.getInputStream()) {

            Files.copy(
                    entrada,
                    destino,
                    StandardCopyOption.REPLACE_EXISTING
            );
        }

        return nomeSeguro;
    }

    public void remover(String nomeArquivo)
        throws IOException {

    if (nomeArquivo == null
            || nomeArquivo.isBlank()) {

        return;
    }

    Path arquivo =
            diretorioUploads
                    .resolve(nomeArquivo)
                    .normalize();

    if (!arquivo.startsWith(diretorioUploads)) {
        throw new IllegalArgumentException();
    }

    Files.deleteIfExists(arquivo);
    }

}