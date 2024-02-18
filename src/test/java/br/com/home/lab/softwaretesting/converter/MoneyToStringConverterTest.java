package  br.com.home.lab.softwaretesting.converter;

import org.assertj.core.api.Assertions;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;

public class MoneyToStringConverterTest  {

    @DataProvider(name = "values")
    protected Object[][] bigDecimalValues(){
        return new Object[][] {
                {null,""},
                {ConvertersHelper.getValue(34), "R$ 34,00"},
                {ConvertersHelper.getValue(8474.84), "R$ 8.474,84"},
                {ConvertersHelper.getValue(3),"R$ 3,00"}
        };
    }


    @Test(dataProvider = "values")
    public void convert(BigDecimal input, String expected){
        MoneyToStringConverter converter = new MoneyToStringConverter();
        String value = converter.convert(input).replaceAll("\\u00a0", " ");
        Assertions.assertThat(value).isEqualTo(expected);
    }
}
