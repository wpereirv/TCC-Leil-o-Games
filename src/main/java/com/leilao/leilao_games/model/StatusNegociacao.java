package com.leilao.leilao_games.model;

public enum StatusNegociacao {

    AGUARDANDO_PAGAMENTO("Aguardando pagamento"),
    AGUARDANDO_ENVIO("Aguardando envio"),
    EM_TRANSPORTE("Produto enviado"),
    CONCLUIDA("Negociação concluída"),
    CANCELADA("Negociação cancelada");

    private final String descricao;

    StatusNegociacao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}