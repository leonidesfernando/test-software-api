package br.com.home.lab.softwaretesting.converter;

import br.com.home.lab.softwaretesting.converter.StringToMoneyConverter;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.ParseException;

import static org.assertj.core.api.Assertions.assertThat;

public class StringToMoneyConverterTest {

    @DataProvider(parallel = true)
    protected Object[][] values(Method method){
        switch (method.getName()){
            case "converterTest" -> {
                return new Object[][]{{null, null},
                        {"R$ 45,75", BigDecimal.valueOf(45.75)},
                        {"R$ 34,45", BigDecimal.valueOf(34.45)},
                        {"", null},
                        {"R$3223", BigDecimal.valueOf(3223)},
                        {"4343", BigDecimal.valueOf(4343)},
                        {"44343,99", BigDecimal.valueOf(44343.99)},
                        {"44.343,99", BigDecimal.valueOf(44343.99)},
                        {"1.044.343,99", BigDecimal.valueOf(1044343.99)},
                        {"R$", null},
                        {"r$", null},

                };
            }
            case "failConversionTest" -> {
                return new Object[][]{{"sdfs"}, {"i34,kk"}};
            }
            default ->
                throw new IllegalArgumentException("Unsupported method " + method.getName());
        }
    }

    @Test(dataProvider = "values")
    public void converterTest(String input, BigDecimal expected){
        StringToMoneyConverter converter = new StringToMoneyConverter();
        assertThat(converter.convert(input)).isEqualTo(expected);

    }

    @Test(dataProvider = "values", expectedExceptions = {ParseException.class})
    public void failConversionTest(String input){
        StringToMoneyConverter converter = new StringToMoneyConverter();
        converter.convert(input);
    }
}
