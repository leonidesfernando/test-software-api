package br.com.home.lab.softwaretesting.payload;

import lombok.Getter;

public class MessageResponse {

    @Getter
    private String message;

    @Getter
    private long id;

    public MessageResponse(String message, long id){
        this.message = message;
        this.id = id;
    }

    public MessageResponse(String message){
        this.message = message;
    }
}
