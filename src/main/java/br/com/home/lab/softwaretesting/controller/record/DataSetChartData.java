package br.com.home.lab.softwaretesting.controller.record;

import java.math.BigDecimal;
import java.util.List;

public record DataSetChartData(List<String> backgroundColor, List<BigDecimal> data) {
}
