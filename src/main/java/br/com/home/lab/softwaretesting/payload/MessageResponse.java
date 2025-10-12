package br.com.home.lab.softwaretesting.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageResponse {

    @Getter
    private String message;

    @Getter
    private Long id;

    public MessageResponse(String message, Long id){
        this.message = message;
        this.id = id;
    }

    public MessageResponse(String message){
        this.message = message;
    }
}
