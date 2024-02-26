package br.com.home.lab.softwaretesting.converter;

import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.text.ParseException;
import java.util.Date;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Execution(ExecutionMode.CONCURRENT)
class StringToDateConverterTest {

    @ParameterizedTest
    @MethodSource("passiveConversionValues")
    void convert(String input, Date expected) {
        StringToDateConverter converter = new StringToDateConverter();
        assertThat(converter.convert(input)).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("invalidConversionValues")
    void invalidConvertions(String input) {
        final StringToDateConverter converter = new StringToDateConverter();
        assertThrows(ParseException.class, () -> converter.convert(input));

    }

    private static Stream<Arguments> passiveConversionValues() {
        return Stream.of(
                Arguments.of("11/11/2020", ConvertersHelper.get(11, 11, 2020)),
                Arguments.of("1/1/2021", ConvertersHelper.get(1, 1, 2021)),
                Arguments.of("01/02/2022", ConvertersHelper.get(1, 2, 2022)),
                Arguments.of("", null), Arguments.of(" ", null),
                Arguments.of(null, null)
        );
    }

    private static Stream<Arguments> invalidConversionValues() {
        return Stream.of(
                Arguments.of("389434"),
                Arguments.of("saklfj"),
                Arguments.of("a44/4/90")
        );
    }
}