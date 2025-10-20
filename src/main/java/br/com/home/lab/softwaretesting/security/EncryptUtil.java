package br.com.home.lab.softwaretesting.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class EncryptUtil {

    public String encode(Object data){
        ObjectMapper mapper = new ObjectMapper();
        try {
            byte[] bytes = mapper.writeValueAsString(data).getBytes(StandardCharsets.UTF_8);
            return Base64.getEncoder().encodeToString(bytes);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    public <T>T decode(String data, Class<T> type){
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(data);
            String jsonString = new String(decodedBytes, StandardCharsets.UTF_8);
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(jsonString, type);
        } catch (Exception e) {
            throw new IllegalStateException("Fail to decode object",e);
        }
    }
}
