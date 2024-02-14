package br.com.home.lab.softwaretesting.controller.record;

import br.com.home.lab.softwaretesting.model.Categoria;
import br.com.home.lab.softwaretesting.model.TipoLancamento;

import java.math.BigDecimal;
public record TotalLancamentoCategoriaRecord(
        BigDecimal total,
        TipoLancamento tipo,
        Categoria categoria
) {}
