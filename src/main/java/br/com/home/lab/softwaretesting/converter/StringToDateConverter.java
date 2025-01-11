package br.com.home.lab.softwaretesting.converter;

import br.com.home.lab.softwaretesting.util.Constantes;
import org.springframework.core.convert.converter.Converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StringToDateConverter implements Converter<String, Date> {

    @Override
    public Date convert(String dateString) {
        if(null == dateString || dateString.isBlank())
            return null;

        String[] dateFormats = {
                Constantes.dd_MM_yyyy_SLASH,
                Constantes.yyyy_MM_dd_SLASH,
                Constantes.yyyy_MMM_dd_DASH
        };

        for (String format : dateFormats) {
            try {
                return new SimpleDateFormat(format).parse(dateString);
            } catch (ParseException ignored) {
                // Try the next format
            }
        }
        return null;
    }
}
