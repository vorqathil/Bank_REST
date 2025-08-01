package com.example.bankcards.exception;

public class UserAlreadyExistsException extends RuntimeException{
    public UserAlreadyExistsException(String message) {
        super("User already exists with username: " + message);
    }
}
