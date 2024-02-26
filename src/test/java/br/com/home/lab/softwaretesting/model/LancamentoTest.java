package br.com.home.lab.softwaretesting.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Execution(ExecutionMode.CONCURRENT)
class LancamentoTest {

    @Test
    void equalsTest(){
        var l1 = new Lancamento();
        var l2 = new Lancamento();
        l1.setId(1);
        l2.setId(1);
        assertThat(l1).isEqualTo(l2);
    }

    @Test
    void notEqualsTest(){
        var l1 = new Lancamento();
        var l2 = new Lancamento();
        l1.setId(2);
        l2.setId(1);

        Long number = 1L;
        assertThat(l1)
                .satisfies(s -> {
                    assertThat(s).isNotEqualTo(l2);
                    assertThat(s.getId()).isNotEqualTo(number);
                });
    }
}
