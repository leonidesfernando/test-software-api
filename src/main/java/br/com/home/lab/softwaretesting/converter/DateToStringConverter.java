package br.com.home.lab.softwaretesting.converter;

import org.springframework.core.convert.converter.Converter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import static br.com.home.lab.softwaretesting.util.Constantes.dd_MM_yyyy_SLASH;

public class DateToStringConverter implements Converter<Date, String> {

    @Override
    public String convert(Date date) {
        return Objects.nonNull(date)
                ? new SimpleDateFormat(dd_MM_yyyy_SLASH).format(date)
                : null;
    }
}
