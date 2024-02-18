package br.com.home.lab.softwaretesting.cucumber.util;

public record Operation(
        int leftOperand,
        int rightOperand,
        int expectedResult) {}
