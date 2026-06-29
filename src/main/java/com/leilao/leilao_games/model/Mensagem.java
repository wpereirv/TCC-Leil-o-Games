package com.leilao.leilao_games.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "mensagens")
public class Mensagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "conversa_id")
    private Conversa conversa;

    @ManyToOne
    @JoinColumn(name = "remetente_id")
    private Usuario remetente;

    @Column(columnDefinition = "TEXT")
    private String texto;

    private LocalDateTime dataHora;

    private Boolean lida = false;

    public Long getId() {
        return id;
    }

    public Conversa getConversa() {
        return conversa;
    }

    public void setConversa(Conversa conversa) {
        this.conversa = conversa;
    }

    public Usuario getRemetente() {
        return remetente;
    }

    public void setRemetente(Usuario remetente) {
        this.remetente = remetente;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public Boolean getLida() {
        return lida;
    }

    public void setLida(Boolean lida) {
        this.lida = lida;
    }

    @Transient
    public String getDataHoraFormatada() {

    if (dataHora == null) {
        return "";
    }

    DateTimeFormatter formato =
            DateTimeFormatter.ofPattern(
                    "dd/MM/yyyy HH:mm"
            );

    return dataHora.format(formato);
    }
}