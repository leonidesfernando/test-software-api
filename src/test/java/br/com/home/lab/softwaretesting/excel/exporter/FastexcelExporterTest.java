package br.com.home.lab.softwaretesting.excel.exporter;

import br.com.home.lab.softwaretesting.controller.record.EntryRecord;
import br.com.home.lab.softwaretesting.controller.record.ResultRecord;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Row;
import org.dhatim.fastexcel.reader.Sheet;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class FastexcelExporterTest {

    private final FastexcelExporter<ResultRecord> exporter = new FastexcelExporter<>();

    enum HEADER {
        DESCRIPTION("Description"),
        CATEGORY("Category"),
        ENTRY_TYPE("Entry type"),
        ENTRY_DATE("Entry date"),
        VALUE("Value");

        private final String headerName;
        HEADER(String headerName) {
            this.headerName = headerName;
        }
    }

    @Test
    void shouldGenerateValidExcelWithData() throws Exception {
        ResultRecord data = getResultRecord();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        exporter.write(data, out);

        byte[] excelBytes = out.toByteArray();
        assertThat(excelBytes).hasSizeGreaterThan(500); // reasonable size

        // Re-open with Fastexcel reader to verify content (lightweight, no POI needed)
        try (ReadableWorkbook wb = new ReadableWorkbook(new ByteArrayInputStream(excelBytes))) {
            Sheet sheet = wb.getFirstSheet(); // First (and only) sheet
            try (Stream<Row> rows = sheet.openStream()) { // Streaming rows
                var rowIterator = rows.iterator();

                    // Header row (row 0)
                Row headerRow = rowIterator.next();
                List<HEADER> headers = List.of(HEADER.values());
                headers.forEach(h -> {
                    int index = h.ordinal();
                    assertThat(headerRow.getCellAsString(index)).contains(h.headerName);
                });

                // First data row (row 1)
                Row firstDataRow = rowIterator.next();
                assertThat(firstDataRow.getCellAsString(HEADER.DESCRIPTION.ordinal())).contains("Desc 1");
                assertThat(firstDataRow.getCellAsString(HEADER.CATEGORY.ordinal())).contains("FOOD");
                assertThat(firstDataRow.getCellAsString(HEADER.ENTRY_TYPE.ordinal())).contains("EXPENSE");
                assertThat(firstDataRow.getCellAsString(HEADER.ENTRY_DATE.ordinal())).contains("2025-11-01"); // Date as string
                assertThat(firstDataRow.getCellAsString(HEADER.VALUE.ordinal())).contains("123");


                // Go till last data row
                for(int i=1; i<=5; i++){
                    rowIterator.next();
                }
                // No more rows
                assertThat(rowIterator.hasNext()).isFalse();
            }
        }
    }

    @Test
    void shouldHandleEmptyDataGracefully() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        exporter.write(getEmptyResultRecord(), out);
        byte[] excelBytes = out.toByteArray();

        try (ReadableWorkbook wb = new ReadableWorkbook(new ByteArrayInputStream(excelBytes))) {
            Sheet sheet = wb.getFirstSheet();
            try (Stream<Row> rows = sheet.openStream()) {
                var rowIterator = rows.iterator();
                assertThat(rowIterator.hasNext()).isTrue(); // At least one row

                Row singleRow = rowIterator.next();
                assertThat(singleRow.getCellAsString(0)).contains("No data to export");

                // No more rows
                assertThat(rowIterator.hasNext()).isFalse();
            }
        }
    }

    @Test
    void testGenerateFileName(){
        String fileName = exporter.generateFileName();
        assertThat(fileName).isNotNull();
        assertThat(fileName).endsWith(".xlsx");
        assertThat(fileName).startsWith("export-");
    }

    @Test
    void testContentTypeAndExtension(){
        assertThat(exporter.getContentType()).isEqualTo("application/octet-stream");
        assertThat(exporter.getFileExtension()).isEqualTo(".xlsx");
    }


    private static ResultRecord getEmptyResultRecord(){
        return new ResultRecord(
                "0", "0", "0", "0", Collections.emptyList(),1,10,0L, Collections.emptyList(),"");
    }

    private static ResultRecord getResultRecord(){
        var entries =                 List.of(
                new EntryRecord(1L, "Desc 1", "123", "2025-11-01","EXPENSE","FOOD", 1l),
                new EntryRecord(2L, "Desc 2", "234", "2025-11-02","EXPENSE","LEISURE", 1l),
                new EntryRecord(3L, "Desc 2", "234", "2025-11-02","EXPENSE","LEISURE", 1l),
                new EntryRecord(4L, "Desc 2", "34", "2025-11-02","EXPENSE","LEISURE", 1l),
                new EntryRecord(5L, "Desc 2", "49", "2025-11-02","EXPENSE","FOOD", 1l),
                new EntryRecord(6L, "Descricao", "2304", "2025-11-02","EXPENSE","FOOD", 1l)
        );
        ResultRecord resultRecord = new ResultRecord(
                "0", "0", "0", "0",entries,1,10,2L,List.of(1,2),"");
        return resultRecord;
    }
}
