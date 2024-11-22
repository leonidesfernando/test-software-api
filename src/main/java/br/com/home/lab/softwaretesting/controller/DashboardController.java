package br.com.home.lab.softwaretesting.controller;

import br.com.home.lab.softwaretesting.controller.record.*;
import br.com.home.lab.softwaretesting.model.TipoLancamento;
import br.com.home.lab.softwaretesting.service.EntryService;
import br.com.home.lab.softwaretesting.util.Constantes;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    @NonNull
    private EntryService entryService;


    protected List<List<Object>> getTableData(Date dataInicial, Date dataFinal){
        List<TotalLancamentoCategoriaRecord> totaisCategoria =  entryService.getTotalPorPeriodoPorCategoria(dataInicial, dataFinal);

        NumberFormat format = new DecimalFormat("'R$ ' #,###,##0.00");
        List<List<Object>> data = new ArrayList<>();
        for(TotalLancamentoCategoriaRecord t : totaisCategoria){
            List<Object> item = new ArrayList<>();

            BigDecimal valor = t.total().setScale(2, RoundingMode.HALF_UP);
            if(TipoLancamento.EXPENSE == t.tipo()){
                valor = valor.negate();
            }
            item.add(t.category().getNome());
            KeyValueTableData keyValue = new KeyValueTableData(valor,format.format(valor));
            item.add(keyValue);
            data.add(item);
        }
        return data;
    }

    protected List<List<Object>> getChartDataDougnt(Date dataInicial, Date dataFinal){

        List<TotalLancamentoRecord> totais =  entryService.getTotalPorPeriodo(dataInicial, dataFinal);
        List<List<Object>> data = new ArrayList<>();
        for(TotalLancamentoRecord t : totais) {
            List<Object> item = new ArrayList<>();
            item.add(t.tipo().getTipo());
            item.add(t.total().setScale(2, RoundingMode.HALF_UP));
            data.add(item);
        }
        return data;
    }


    private Date initialDate(Date dataInicial){
        if(dataInicial == null) {
            dataInicial = Date.from(LocalDate.now()
                    .with(TemporalAdjusters.firstDayOfMonth())
                    .atStartOfDay()
                    .atZone(ZoneId.systemDefault()).toInstant());
        }
        return dataInicial;
    }

    private Date finalDate(Date dataFinal){
        if(dataFinal == null) {
            dataFinal = Date.from(LocalDate.now()
                    .with(TemporalAdjusters.lastDayOfMonth())
                    .atStartOfDay()
                    .atZone(ZoneId.systemDefault()).toInstant());
        }
        return dataFinal;
    }

    @RequestMapping("/tableChart")
    public ResponseEntity<List<List<Object>>> tableData(@RequestParam("startDate")
                                           @DateTimeFormat(pattern = Constantes.yyyy_MMM_dd_DASH)
                                           Date dataInicial,
                                       @RequestParam("endDate")
                                           @DateTimeFormat(pattern = Constantes.yyyy_MMM_dd_DASH)
                                           Date dataFinal){
        return ResponseEntity.ok(getTableData(initialDate(dataInicial), finalDate(dataFinal)));
    }


    @RequestMapping("/doughnutChart")
    public ResponseEntity<List<List<Object>>> doughnutChart(@RequestParam("startDate")
                                           @DateTimeFormat(pattern = Constantes.yyyy_MMM_dd_DASH)
                                            Date dataInicial,
                                           @RequestParam("endDate")
                                           @DateTimeFormat(pattern = Constantes.yyyy_MMM_dd_DASH)
                                            Date dataFinal){

        return ResponseEntity.ok(getChartDataDougnt(dataInicial, dataFinal));
    }

}
