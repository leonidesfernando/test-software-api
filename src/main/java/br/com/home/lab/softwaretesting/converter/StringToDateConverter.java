package br.com.home.lab.softwaretesting.converter;

import br.com.home.lab.softwaretesting.util.Constantes;
import lombok.SneakyThrows;
import org.springframework.core.convert.converter.Converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static br.com.home.lab.softwaretesting.util.Constantes.yyyy_MMM_dd_DASH;

public class StringToDateConverter implements Converter<String, Date> {

    @SneakyThrows
    @Override
    public Date convert(String dateString) {
        if(null == dateString || dateString.isBlank())
            return null;
        try {
            return new SimpleDateFormat(Constantes.dd_MM_yyyy_SLASH).parse(dateString);
        }catch (ParseException e){
            try {
                return new SimpleDateFormat(Constantes.yyyy_MM_dd_SLASH).parse(dateString);
            }catch (ParseException ee){
                return  new SimpleDateFormat(yyyy_MMM_dd_DASH).parse(dateString);
            }
        }
    }
}
