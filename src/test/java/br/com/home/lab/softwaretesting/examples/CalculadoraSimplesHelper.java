package br.com.home.lab.softwaretesting.examples;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CalculadoraSimplesHelper {

    public static int soma(int s1, int s2){
        return s1 + s2;
    }

    public static BigDecimal subtrai(BigDecimal d1, BigDecimal d2){
        return d1.subtract(d2).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal soma(BigDecimal d1, BigDecimal d2){
        return d1.add(d2).setScale(2,RoundingMode.HALF_UP);
    }

    public static BigDecimal divide(BigDecimal d1, BigDecimal d2){
        return d1.divide(d2, 2, RoundingMode.HALF_UP);
    }

    public static BigDecimal multiplica(BigDecimal d1, BigDecimal d2){
        return d1.multiply(d2).setScale(2, RoundingMode.HALF_UP);
    }
}



