package br.com.home.lab.softwaretesting.converter;

import lombok.SneakyThrows;
import org.springframework.core.convert.converter.Converter;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;

import static br.com.home.lab.softwaretesting.util.Constantes.BR;

public class StringToMoneyConverter implements Converter<String, BigDecimal> {

    @SneakyThrows
    @Override
    public BigDecimal convert(String numberString) {

        if(!StringUtils.hasText(numberString) || List.of("", "R$", "r$").contains(numberString.trim())){
            return null;
        }

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(BR);
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        String pattern = "#,##0.0#";
        DecimalFormat decimalFormat = new DecimalFormat(pattern, symbols);
        decimalFormat.setParseBigDecimal(true);

        return new BigDecimal(decimalFormat.parse(numberString.replaceAll("\\u00a0", "")
                .replaceAll("\\s*", "")
                .replaceAll("�", "")
                .replace("R$", ""))
                .toString());

    }
}
