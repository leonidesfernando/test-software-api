package br.com.home.lab.softwaretesting.cucumber.definitions;

import br.com.home.lab.softwaretesting.cucumber.util.Operation;
import io.cucumber.java.DataTableType;
import io.cucumber.java.en.Given;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static br.com.home.lab.softwaretesting.examples.CalculadoraSimplesHelper.*;
import static org.assertj.core.api.Assertions.assertThat;

public class CalculadoraStepDefinitions {

    @DataTableType
    public Operation operation(Map<String, String> row) {
        return new Operation(
                Integer.parseInt(row.get("leftOperand")),
                Integer.parseInt(row.get("rightOperand")),
                Integer.parseInt(row.get("expected")));
    }

    @Given("Quero calcular a soma dos seguintes numeros")
    public void quero_calcular_a_soma_dos_seguintes_numeros(List<Operation> dataTable) {
        dataTable.forEach(operation ->
                assertThat(soma(operation.leftOperand(), operation.rightOperand()))
                        .isEqualByComparingTo(operation.expectedResult())
        );

    }

    @Given("Quero calcular a multiplicacao de {bigdecimal} por {bigdecimal} que deve resultar em {bigdecimal}")
    public void quero_calcular_a_multiplicacao_de_por_que_deve_resultar_em(BigDecimal fator1, BigDecimal fator2, BigDecimal produto) {
        assertThat(multiplica(fator1,fator2)).isEqualByComparingTo(produto);
    }

    @Given("Quero calcular a divisao de {bigdecimal} por {bigdecimal} que deve resultar em {bigdecimal}")
    public void quero_calcular_a_divisao_de_por_que_deve_resultar_em(BigDecimal dividendo, BigDecimal divisor, BigDecimal quociente) {
        assertThat(divide(dividendo, divisor)).isEqualByComparingTo(quociente);
    }
}
