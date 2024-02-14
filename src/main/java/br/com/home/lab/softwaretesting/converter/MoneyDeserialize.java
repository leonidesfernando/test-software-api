package br.com.home.lab.softwaretesting.converter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

public class MoneyDeserialize extends JsonDeserializer<BigDecimal> {

    @Override
    public BigDecimal deserialize(JsonParser jsonParser,
                                  DeserializationContext deserializationContext) throws IOException {

        String valorString = jsonParser.getText();
        List<String> values = List.of("", "R$");
        if (Objects.isNull(valorString) || values.contains(valorString)) return null;

        final BigDecimal valor = new BigDecimal(valorString
                .replaceAll("\\u00a0", "")
                .replaceAll("\\s*", "")
                .replace(".", "")
                .replace(",", ".")
                .replace("R$", "")
                .trim());
        return valor.setScale(2, RoundingMode.HALF_UP);
    }
}
