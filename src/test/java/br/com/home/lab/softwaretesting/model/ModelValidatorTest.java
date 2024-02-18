package br.com.home.lab.softwaretesting.model;

import br.com.home.lab.softwaretesting.util.LancamentoGen;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
public class ModelValidatorTest {

    @Mock
    private ModelValidator modelValidator;

    @BeforeClass
    public void init(){
        MockitoAnnotations.initMocks(this);
    }

    @Test(enabled = false)
    public void getInstanceTest(){
        when(ModelValidator.getInstance()).thenCallRealMethod();
        assertThat(ModelValidator.getInstance()).isNotNull();
    }

    @Test
    public void createValidatorTest(){
        when(modelValidator.createValidator()).thenCallRealMethod();
        assertThat(modelValidator.createValidator()).isNotNull();
    }

    @Test
    public void validate(){
        var lancamento = LancamentoGen.novaDespesa();
        ModelValidator.getInstance().validate(lancamento);
    }
}
