package br.com.home.lab.softwaretesting.controller.record;

import br.com.home.lab.softwaretesting.model.TipoLancamento;

import java.math.BigDecimal;

public record TotalLancamentoRecord(
        BigDecimal total,
        TipoLancamento tipo
) {
}
