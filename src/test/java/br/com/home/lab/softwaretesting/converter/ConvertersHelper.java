package br.com.home.lab.softwaretesting.converter;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static java.math.BigDecimal.valueOf;
import static java.util.Date.from;

@UtilityClass
public class ConvertersHelper {
    public BigDecimal getValue(double value){
        return valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }

    public Date get(int dia, int mes, int ano){
        return from(Instant.from(
                LocalDate.of(ano, mes, dia)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()));
    }
}
