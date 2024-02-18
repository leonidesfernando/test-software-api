package br.com.home.lab.softwaretesting.cucumber.util;

import io.cucumber.datatable.DataTable;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class DataTableUtil {

    public List<String> getLine(DataTable table, int index) {
        checkIndex(table, index);
        return table.row(index);
    }

    public List<Integer> geLineAsInt(DataTable table, int index) {
        List<String> list = getLine(table, index);
        return list.stream().map(Integer::parseInt).collect(Collectors.toList());
    }

    private void checkIndex(DataTable table, int index) {
        if (index > table.height())
            throw new IllegalArgumentException(String.format("O indice da linha é maior que o total de linhas da tabela",
                    index, table.height()));
    }
}
