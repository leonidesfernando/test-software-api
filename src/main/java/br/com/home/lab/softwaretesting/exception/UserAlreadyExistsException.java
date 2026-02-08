package br.com.home.lab.softwaretesting.exception;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String username) {
        super(username);
    }
}
