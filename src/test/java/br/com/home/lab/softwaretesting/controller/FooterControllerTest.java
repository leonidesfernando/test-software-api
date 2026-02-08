package br.com.home.lab.softwaretesting.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class FooterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/footer/env returns environment name from properties")
    void shouldReturnEnv() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/footer/env"))
                .andExpect(status().isOk())
                .andExpect(content().string("Test"));
    }

    @Test
    @DisplayName("GET /api/footer/dbName returns database name from properties")
    void shouldReturnDbName() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/footer/dbName"))
                .andExpect(status().isOk())
                .andExpect(content().string("teste-software"));
    }

    @Test
    @DisplayName("GET /api/footer/dbVendor returns database vendor from properties")
    void shouldReturnDbVendor() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/footer/dbVendor"))
                .andExpect(status().isOk())
                .andExpect(content().string("Postgres"));
    }
}
