package br.com.home.lab.softwaretesting.controller.record;

import java.util.List;

public record ResultadoRecord(
        String totalSaida,
        String totalEntrada,
        String totalGeralSaida,
        String totalGeralEntrada,
        List<LancamentoRecord>lancamentos,
        int p,
        int tamanhoPagina,
        long totalRegistros,
        String itemBusca
) { }
