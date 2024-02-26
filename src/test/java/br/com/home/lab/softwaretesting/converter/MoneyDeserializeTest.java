package br.com.home.lab.softwaretesting.converter;

import com.fasterxml.jackson.core.JsonParser;
import lombok.SneakyThrows;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;


@Execution(ExecutionMode.CONCURRENT)
class MoneyDeserializeTest {

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("createData")
    void deserialize(String input, BigDecimal expected) {
        JsonParser jsonParser = mock(JsonParser.class);
        MoneyDeserialize moneyDeserialize = new MoneyDeserialize();
        given(jsonParser.getText()).willReturn(input);

        //when
        BigDecimal actual = moneyDeserialize.deserialize(jsonParser, null);

        //then
        assertEquals(actual, expected);
        assertThat(actual).isEqualTo(expected);
    }

    private static Stream<Arguments> createData() {
        return Stream.of(
                Arguments.of("", null),
                Arguments.of(null, null),
                Arguments.of("R$", null),
                Arguments.of("34", ConvertersHelper.getValue(34)),
                Arguments.of("R$ 8474,84", ConvertersHelper.getValue(8474.84)),
                Arguments.of("R$3", ConvertersHelper.getValue(3))
        );
    }
}
