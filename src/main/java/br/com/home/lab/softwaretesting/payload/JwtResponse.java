package br.com.home.lab.softwaretesting.payload;

import java.util.List;


public record JwtResponse(String token, String type, Long id, String name, String username, String email, List<String> roles) {
}
