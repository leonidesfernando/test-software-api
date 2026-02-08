package br.com.home.lab.softwaretesting.excel.exporter;


import br.com.home.lab.softwaretesting.controller.record.EntryRecord;
import br.com.home.lab.softwaretesting.controller.record.ResultRecord;
import org.dhatim.fastexcel.BorderStyle;
import org.dhatim.fastexcel.Workbook;
import org.dhatim.fastexcel.Worksheet;
import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.util.List;

import static br.com.home.lab.softwaretesting.excel.exporter.FastexcelExporter.ResultHeader.CATEGORY;
import static br.com.home.lab.softwaretesting.excel.exporter.FastexcelExporter.ResultHeader.DESCRIPTION;
import static br.com.home.lab.softwaretesting.excel.exporter.FastexcelExporter.ResultHeader.ENTRY_DATE;
import static br.com.home.lab.softwaretesting.excel.exporter.FastexcelExporter.ResultHeader.ENTRY_TYPE;
import static br.com.home.lab.softwaretesting.excel.exporter.FastexcelExporter.ResultHeader.VALUE;


@Component
public class FastexcelExporter<T> implements ExcelExporter<T> {

    private static final String[] RESULT_HEADERS = {"Description", "Category", "Entry type", "Entry date", "Value"};

    public enum ResultHeader {
        DESCRIPTION,
        CATEGORY,
        ENTRY_TYPE,
        ENTRY_DATE,
        VALUE;
    }

    @Override
    public String generateFileName() {
        return DEFAULT_FILE_NAME_PREFIX + "-" + java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + getFileExtension();
    }

    @Override
    public void write(ResultRecord data, OutputStream out) throws Exception {
        try (
                Workbook wb = new Workbook(out, "MyApp", "1.0");
                Worksheet ws = wb.newWorksheet("Data")
        ){

            if (data.entries().isEmpty()) {
                ws.value(0, 0, "No data to export");
                return;
            }
            writePersonData(ws, data);
        }
    }

    private void writePersonData(Worksheet ws, ResultRecord result) {
        // Header
        for (int col = 0; col < RESULT_HEADERS.length; col++) {
            ws.value(0, col, RESULT_HEADERS[col]);
        }
        ws.range(0, 0, 0, RESULT_HEADERS.length - 1).style()
                .bold()
                .fillColor("F0F0F0")
                .horizontalAlignment("center")
                .borderStyle(BorderStyle.THIN)
                .set();

        // Data rows
        List<EntryRecord> list = result.entries();

        int index =  1;
        for(EntryRecord entry : list) {
            ws.value(index, DESCRIPTION.ordinal(), entry.description());
            ws.value(index, CATEGORY.ordinal(), entry.category());
            ws.value(index, ENTRY_TYPE.ordinal(), entry.entryType());
            ws.value(index, ENTRY_DATE.ordinal(), entry.entryDate());
            ws.value(index, VALUE.ordinal(), entry.amount());
            index++;
        }

//        int nColumns = values().length;
        // Freeze header index
//        ws.range(1, 0, nColumns - 1, RESULT_HEADERS.length - 1).freezePanes(1, 0);
        //ws.range(1, 0, nColumns - 1, RESULT_HEADERS.length - 1).freezePanes(1, 0);
//        ws.freezePane(nColumns, 0);
    }
}