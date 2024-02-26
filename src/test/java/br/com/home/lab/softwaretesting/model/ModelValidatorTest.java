package br.com.home.lab.softwaretesting.model;

import br.com.home.lab.softwaretesting.util.LancamentoGen;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
@Execution(ExecutionMode.CONCURRENT)
class ModelValidatorTest {

    @Mock
    private ModelValidator modelValidator;

    @BeforeAll
    public void init(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @Disabled
    void getInstanceTest(){
        when(ModelValidator.getInstance()).thenCallRealMethod();
        assertThat(ModelValidator.getInstance()).isNotNull();
    }

    @Test
    void createValidatorTest(){
        when(modelValidator.createValidator()).thenCallRealMethod();
        assertThat(modelValidator.createValidator()).isNotNull();
    }

    @Test
    void validate(){
        var lancamento = LancamentoGen.novaDespesa();
        ModelValidator.getInstance().validate(lancamento);
    }
}
