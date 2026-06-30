package com.leilao.leilao_games.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "produtos")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "comprador_id")
    private Usuario comprador;

    private Boolean avaliado = false;

    private String descricao;

    private Double valorInicial;

    private String imagem1;

    private String imagem2;

    private String imagem3;

    private LocalDateTime dataInicio;

    private LocalDateTime dataFim;

    private Boolean encerrado = false;

    @Enumerated(EnumType.STRING)
    private StatusNegociacao statusNegociacao;

    private Double valorFinal;

    private String codigoRastreio;

    private LocalDateTime dataPagamento;

    private LocalDateTime dataEnvio;

    private LocalDateTime dataConclusao;    

    @OneToMany(mappedBy = "produto")
    private List<Lance> lances;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Usuario getComprador() {
        return comprador;
    }

    public void setComprador(Usuario comprador) {
        this.comprador = comprador;
    }

    public Boolean getAvaliado() {
        return avaliado;
    }

    public void setAvaliado(Boolean avaliado) {
        this.avaliado = avaliado;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Double getValorInicial() {
        return valorInicial;
    }

    public void setValorInicial(Double valorInicial) {
        this.valorInicial = valorInicial;
    }

    public String getImagem1() {
        return imagem1;
    }

    public void setImagem1(String imagem1) {
        this.imagem1 = imagem1;
    }

    public String getImagem2() {
        return imagem2;
    }

    public void setImagem2(String imagem2) {
        this.imagem2 = imagem2;
    }

    public String getImagem3() {
        return imagem3;
    }

    public void setImagem3(String imagem3) {
        this.imagem3 = imagem3;
    }

    public List<Lance> getLances() {
        return lances;
    }

    public void setLances(List<Lance> lances) {
        this.lances = lances;
    }

    public LocalDateTime getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDateTime dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDateTime getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDateTime dataFim) {
        this.dataFim = dataFim;
    }

    public Boolean getEncerrado() {
        return encerrado;
    }

    public void setEncerrado(Boolean encerrado) {
        this.encerrado = encerrado;
    }

    public StatusNegociacao getStatusNegociacao() {
    return statusNegociacao;
}

public void setStatusNegociacao(
        StatusNegociacao statusNegociacao) {

    this.statusNegociacao = statusNegociacao;
}

public Double getValorFinal() {
    return valorFinal;
}

public void setValorFinal(Double valorFinal) {
    this.valorFinal = valorFinal;
}

public String getCodigoRastreio() {
    return codigoRastreio;
}

public void setCodigoRastreio(
        String codigoRastreio) {

    this.codigoRastreio = codigoRastreio;
}

public LocalDateTime getDataPagamento() {
    return dataPagamento;
}

public void setDataPagamento(
        LocalDateTime dataPagamento) {

    this.dataPagamento = dataPagamento;
}

public LocalDateTime getDataEnvio() {
    return dataEnvio;
}

public void setDataEnvio(LocalDateTime dataEnvio) {
    this.dataEnvio = dataEnvio;
}

public LocalDateTime getDataConclusao() {
    return dataConclusao;
}

public void setDataConclusao(
        LocalDateTime dataConclusao) {

    this.dataConclusao = dataConclusao;
}
}