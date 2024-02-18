package br.com.home.lab.softwaretesting.model;

import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class LancamentoTest {

    @Test
    public void equalsTest(){
        var l1 = new Lancamento();
        var l2 = new Lancamento();
        l1.setId(1);
        l2.setId(1);
        assertThat(l1).isEqualTo(l2);
    }

    @Test
    public void notEqualsTest(){
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
