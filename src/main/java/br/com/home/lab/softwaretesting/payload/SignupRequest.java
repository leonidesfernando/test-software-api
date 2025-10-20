package br.com.home.lab.softwaretesting.payload;

import java.util.Set;

public record SignupRequest(String name, String username, String email, String password, Set<String> roles) {
}
