package br.com.home.lab.softwaretesting.security;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;

@Execution(ExecutionMode.CONCURRENT)
public class EncriptyUtilTest {

    class NonSerializable {
        private NonSerializable self = this;
    }

    @ParameterizedTest
    @MethodSource("dataAndEcnryptValues")
    void validEncryption(String input, String expedted) {
        final EncryptUtil encryptUtil = new EncryptUtil();
        Assertions.assertEquals(encryptUtil.encode(input), expedted);
    }

    @Test
    void invalidEncryption(){
        final EncryptUtil encryptUtil = new EncryptUtil();
        final NonSerializable data = new NonSerializable();
        assertThrows(IllegalStateException.class, () -> encryptUtil.encode(data));
    }


    @ParameterizedTest
    @MethodSource("dataAndEcnryptValues")
    <T> void validDencryption(String data, String encoded, Class<T> tClass) {
        final EncryptUtil encryptUtil = new EncryptUtil();
        Assertions.assertEquals(encryptUtil.decode(encoded, tClass),data);
    }

    @Test
    void invalidDencryption(){
        final EncryptUtil encryptUtil = new EncryptUtil();
        //final NonSerializable data = new NonSerializable();
        assertThrows(IllegalStateException.class, () -> encryptUtil.decode("invalid json", NonSerializable.class));
    }

    private static Stream<Arguments> dataAndEcnryptValues() {
        return Stream.of(
                Arguments.of("11/11/2020", "IjExLzExLzIwMjAi", String.class),
                Arguments.of("""
                        {
                          "name": "John Doe",
                          "age": 30,
                          "isMarried": false,
                          "children": ["Alice", "Bob"],
                          "address": {
                            "street": "123 Main St",
                            "city": "Anytown",
                            "zipCode": "12345"
                          }
                        }
                        """, "IntcbiAgXCJuYW1lXCI6IFwiSm9obiBEb2VcIixcbiAgXCJhZ2VcIjogMzAsXG4gIFwiaXNNYXJyaWVkXCI6IGZhbHNlLFxuICBcImNoaWxkcmVuXCI6IFtcIkFsaWNlXCIsIFwiQm9iXCJdLFxuICBcImFkZHJlc3NcIjoge1xuICAgIFwic3RyZWV0XCI6IFwiMTIzIE1haW4gU3RcIixcbiAgICBcImNpdHlcIjogXCJBbnl0b3duXCIsXG4gICAgXCJ6aXBDb2RlXCI6IFwiMTIzNDVcIlxuICB9XG59XG4i", String.class)
        );
    }
}
