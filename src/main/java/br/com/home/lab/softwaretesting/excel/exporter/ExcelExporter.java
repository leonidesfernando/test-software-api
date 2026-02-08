package br.com.home.lab.softwaretesting.excel.exporter;

import br.com.home.lab.softwaretesting.controller.record.ResultRecord;
import org.springframework.http.MediaType;

import java.io.OutputStream;

public interface ExcelExporter<T> {
    String DEFAULT_FILE_NAME_PREFIX = "export";

    void write(ResultRecord data, OutputStream out) throws Exception;

    default String getContentType() {
        return MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }

    default String getFileExtension() {
        return ".xlsx";
    }

    String generateFileName();
}