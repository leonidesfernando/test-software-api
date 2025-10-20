package br.com.home.lab.softwaretesting.controller.record;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

public record ResultRecord(
        String totalSpend,
        String totalEarnings,
        String grandTotalExpenses,
        String grandTotalWinnings,
        List<EntryRecord> entries,
        int p,
        int pageSize,
        long totalRecords,
        List<Integer> pages,
        String itemSearch
) {

    public String encrypt() {
        //TODO: use EncryptUtil
        ObjectMapper mapper = new ObjectMapper();
        try {
            byte[] bytes = mapper.writeValueAsString(this).getBytes(StandardCharsets.UTF_8);
            return Base64.getEncoder().encodeToString(bytes);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
