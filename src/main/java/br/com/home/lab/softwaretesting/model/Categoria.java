package br.com.home.lab.softwaretesting.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum Categoria {

    @JsonProperty("food")
    FOOD("food"),
    @JsonProperty("wage")
    WAGE("wage"),
    @JsonProperty("leisure")
    LEISURE("leisure"),
    @JsonProperty("phone.internet")
    PHONE_INTERNET ("phone.internet"),
    @JsonProperty("car")
    CAR ("car"),
    @JsonProperty("loan")
    LOAN("loan"),
    @JsonProperty("investments")
    INVESTMENTS("investments"),
    @JsonProperty("clothing")
    CLOTHING("clothing"),
    @JsonProperty("other")
    OTHER("other");


    @Getter
    private String nome;
}
