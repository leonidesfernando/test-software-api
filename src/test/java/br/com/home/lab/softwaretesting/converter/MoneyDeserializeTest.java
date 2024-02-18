package br.com.home.lab.softwaretesting.converter;

import com.fasterxml.jackson.core.JsonParser;
import lombok.SneakyThrows;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;


public class MoneyDeserializeTest {

    @DataProvider(name = "values")
    protected Object[][] createData() {
        return new Object[][]{
                {"", null},
                {null, null},
                {"R$", null},
                {"34", ConvertersHelper.getValue(34)},
                {"R$ 8474,84", ConvertersHelper.getValue(8474.84)},
                {"R$3", ConvertersHelper.getValue(3)}
        };
    }

    @SneakyThrows
    @Test(dataProvider = "values")
    public void deserialize(String input, BigDecimal expected) {
        JsonParser jsonParser = mock(JsonParser.class);
        MoneyDeserialize moneyDeserialize = new MoneyDeserialize();
        given(jsonParser.getText()).willReturn(input);

        //when
        BigDecimal actual = moneyDeserialize.deserialize(jsonParser, null);

        //then
        assertEquals(actual, expected);
        assertThat(actual).isEqualTo(expected);
    }
}
