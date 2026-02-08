package br.com.home.lab.softwaretesting.exception;


public class RoleNotFoundException extends RuntimeException {
    public RoleNotFoundException(String role) {
        super(role);
    }
}
