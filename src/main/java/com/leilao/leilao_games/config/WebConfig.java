package com.leilao.leilao_games.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig
        implements WebMvcConfigurer {

    private final AdminInterceptor adminInterceptor;

    private final Path diretorioUploads;

    public WebConfig(
            AdminInterceptor adminInterceptor,

            @Value("${app.upload.dir:uploads}")
            String uploadDir) {

        this.adminInterceptor = adminInterceptor;

        this.diretorioUploads =
                Paths.get(uploadDir)
                        .toAbsolutePath()
                        .normalize();
    }

    @Override
    public void addInterceptors(
            InterceptorRegistry registry) {

        registry.addInterceptor(adminInterceptor)
                .addPathPatterns(
                        "/dashboard",
                        "/admin/**"
                );
    }

    @Override
    public void addResourceHandlers(
            ResourceHandlerRegistry registry) {

        String localizacaoUploads =
                diretorioUploads.toUri().toString();

        if (!localizacaoUploads.endsWith("/")) {
            localizacaoUploads += "/";
        }

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(localizacaoUploads);
    }
}