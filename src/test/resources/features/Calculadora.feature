Feature: Calculadora - validar as operações de soma, subtracao, multiplicacao e divisao
  Cenario a ser validado:
  1. soma de números positivos
  2. soma de números negativos
  3. soma de números positivos e negativos
  4. multiplicacao
  5 divisao

  Scenario: Somar dois numeros positivos
    Given Quero calcular a soma dos seguintes numeros
      | leftOperand | rightOperand | expected |
      | 2           | 22           | 24       |
      | 234         | 454          | 688      |
      | 24          | 4554         | 4578     |
      | 2234        | 45554        | 47788    |

  Scenario: Somar dois numeros negativos e positivos
    Given Quero calcular a soma dos seguintes numeros
      | leftOperand | rightOperand | expected |
      | -3          | -4           | -7       |
      | -30         | 14           | -16      |
      | -3343       | -408         | -3751    |
      | -333        | 408          | 75       |

  Scenario Outline: Multiplicar 2 números
    Given Quero calcular a multiplicacao de <fator1> por <fator2> que deve resultar em <produto>
    Examples:
      | fator1 | fator2 | produto |
      | 1      | 1      | 1.00    |
      | 2.5    | 5      | 12.50   |
      | 100    | 1.0    | 100.00  |
      | 8      | 6      | 48.00   |
      | 3.2    | 4.9    | 15.68   |

  Scenario Outline: Dividir 2 números
    Given Quero calcular a divisao de <dividendo> por <divisor> que deve resultar em <quociente>
    Examples:
      | dividendo | divisor | quociente |
      | 1         | 1       | 1.00      |
      | 5         | 2.5     | 2.00      |
      | 100       | 1.0     | 100.00    |
      | 48        | 6       | 8.00      |
      | 3.2       | 4.9     | 0.65      |