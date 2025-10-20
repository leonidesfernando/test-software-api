package  br.com.home.lab.softwaretesting.converter;

import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@Execution(ExecutionMode.CONCURRENT)
class MoneyToStringConverterTest  {

    @ParameterizedTest
    @MethodSource("bigDecimalValues")
    void convert(BigDecimal input, String expected) {
        MoneyToStringConverter converter = new MoneyToStringConverter();
        String value = converter.convert(input).replaceAll("\\u00a0", " ");
        assertThat(value).isEqualTo(expected);
    }

    private static Stream<Arguments> bigDecimalValues() {
        return Stream.of(
                Arguments.of(null, ""),
                Arguments.of(ConvertersHelper.getValue(34), "R$ 34,00"),
                Arguments.of(ConvertersHelper.getValue(8474.84), "R$ 8.474,84"),
                Arguments.of(ConvertersHelper.getValue(3), "R$ 3,00")
        );
    }
}
