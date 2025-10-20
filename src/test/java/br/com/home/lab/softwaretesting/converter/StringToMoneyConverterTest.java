package br.com.home.lab.softwaretesting.converter;

import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Execution(ExecutionMode.CONCURRENT)
class StringToMoneyConverterTest {

    @ParameterizedTest
    @MethodSource("converterData")
    void converterTest(String input, BigDecimal expected) {
        StringToMoneyConverter converter = new StringToMoneyConverter();
        assertThat(converter.convert(input)).isEqualTo(expected);

    }

    @ParameterizedTest
    @MethodSource("failConversionData")
    void failConversionTest(String input) {
        final StringToMoneyConverter converter = new StringToMoneyConverter();
        assertThrows(ParseException.class, () ->
                converter.convert(input)
        );
    }

    private static Stream<Arguments> converterData() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of("R$ 45,75", BigDecimal.valueOf(45.75)),
                Arguments.of("R$ 34,45", BigDecimal.valueOf(34.45)),
                Arguments.of("", null),
                Arguments.of("R$3223", BigDecimal.valueOf(3223)),
                Arguments.of("4343", BigDecimal.valueOf(4343)),
                Arguments.of("44343,99", BigDecimal.valueOf(44343.99)),
                Arguments.of("44.343,99", BigDecimal.valueOf(44343.99)),
                Arguments.of("1.044.343,99", BigDecimal.valueOf(1044343.99)),
                Arguments.of("R$", null),
                Arguments.of("r$", null)
        );
    }

    private static Stream<Arguments> failConversionData() {
        return Stream.of(
                Arguments.of("sdfs"),
                Arguments.of("i34,kk")
        );
    }
}
