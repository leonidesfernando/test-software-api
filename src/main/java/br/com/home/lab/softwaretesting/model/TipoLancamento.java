package br.com.home.lab.softwaretesting.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum TipoLancamento {

/*
    //TODO: fazer o esquema da enum traduzida no projeto gestor - perseu store
            e fazer na categoria tambem*/
    @JsonProperty("income")
    INCOME("income"),
    @JsonProperty("expense")
    EXPENSE("expense"),
    @JsonProperty("transf")
    TRANSF("transf");
    @Getter
    private String tipo;
}
