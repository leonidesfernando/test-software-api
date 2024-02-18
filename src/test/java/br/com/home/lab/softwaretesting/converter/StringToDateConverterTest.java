package br.com.home.lab.softwaretesting.converter;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class StringToDateConverterTest {

    @DataProvider(name = "values", parallel = true)
    protected Object[][] values(Method method){
        switch (method.getName()){
            case "convert" -> {
                return new Object[][]{
                        {"11/11/2020", ConvertersHelper.get(11, 11, 2020)},
                        {"1/1/2021", ConvertersHelper.get(1, 1, 2021)},
                        {"01/02/2022", ConvertersHelper.get(1, 2, 2022)},
                        {"", null},
                        {" ", null},
                        {null, null}
                };
            }
            case "invalidConvertions" -> {
                return new Object[][]{{"389434"}, {"saklfj"}, {"a44/4/90"}};
            }
            default ->
                throw new IllegalArgumentException("Unsupported method: " + method.getName());
        }
    }

    @Test(dataProvider = "values")
    public void convert(String input, Date expected){
        StringToDateConverter converter = new StringToDateConverter();
        assertThat(converter.convert(input)).isEqualTo(expected);
    }

    @Test(dataProvider = "values", expectedExceptions = ParseException.class)
    public void invalidConvertions(String input){
        StringToDateConverter converter = new StringToDateConverter();
        converter.convert(input);
    }
}
