package br.com.home.lab.softwaretesting.controller;

import br.com.home.lab.softwaretesting.model.Category;
import br.com.home.lab.softwaretesting.model.TipoLancamento;
import br.com.home.lab.softwaretesting.security.jwt.AuthTokenFilter;
import br.com.home.lab.softwaretesting.security.jwt.JwtUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = EntryInfoController.class)
@AutoConfigureMockMvc(addFilters = false)
class EntryInfoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private AuthTokenFilter authTokenFilter;

    @Test
    @DisplayName("GET /api/entryInfo/categories returns all categories from enum")
    void shouldReturnAllCategories() throws Exception {

        List<String> expectedCategories =
                Arrays.stream(Category.values())
                        .map(Category::getNome)
                        .map(String::toLowerCase)
                        .toList();

        mockMvc.perform(get("/api/entryInfo/categories"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(expectedCategories.size())))
                .andExpect(jsonPath("$", containsInAnyOrder(
                        expectedCategories.toArray()
                )));
    }

    @Test
    @DisplayName("GET /api/entryInfo/entryTypes returns all entry types from enum")
    void shouldReturnAllEntryTypes() throws Exception {

        List<String> expectedEntryTypes =
                Arrays.stream(TipoLancamento.values())
                        .map(TipoLancamento::getTipo)
                        .map(String::toLowerCase)
                        .toList();

        mockMvc.perform(get("/api/entryInfo/entryTypes"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(expectedEntryTypes.size())))
                .andExpect(jsonPath("$", containsInAnyOrder(
                        expectedEntryTypes.toArray()
                )));
    }
}
