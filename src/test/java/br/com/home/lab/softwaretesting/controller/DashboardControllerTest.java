package br.com.home.lab.softwaretesting.controller;


import br.com.home.lab.softwaretesting.controller.record.TotalLancamentoCategoriaRecord;
import br.com.home.lab.softwaretesting.controller.record.TotalLancamentoRecord;
import br.com.home.lab.softwaretesting.model.Category;
import br.com.home.lab.softwaretesting.model.TipoLancamento;
import br.com.home.lab.softwaretesting.security.jwt.AuthTokenFilter;
import br.com.home.lab.softwaretesting.service.EntryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(
        controllers = DashboardController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = AuthTokenFilter.class
        )
)
@AutoConfigureMockMvc(addFilters = false)
public class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EntryService entryService;

    private static final String userName = "testuser";
    private static final String API_DASHBOARD = "/api/dashboard";
    private static final String TABLE_CHART = API_DASHBOARD + "/tableChart";
    private static final String DOUGHNUT_CHART = API_DASHBOARD + "/doughnutChart";


    @Test
    @DisplayName("GET " + TABLE_CHART + " returns table data for the dashboard")
    @WithMockUser(username = userName, roles = {"USER"})
    void tableDataShouldReturnData() throws Exception {

        when(entryService.getTotalPorPeriodoPorCategoria(any(Date.class), any(Date.class)))
                .thenReturn(genTableData());
        mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(TABLE_CHART)
                        .param("startDate", "2025-12-01")
                        .param("endDate", "2025-12-31")
                 )
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    @DisplayName("GET " + TABLE_CHART + " returns table data for the dashboard without date parameters")
    @WithMockUser(username = userName, roles = {"USER"})
    void tableDataShouldReturnDataWithoutDate() throws Exception {
        when(entryService.getTotalPorPeriodoPorCategoria(any(Date.class), any(Date.class)))
                .thenReturn(genTableData());
        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(TABLE_CHART)
                )
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isNotEmpty());
    }


    @Test
    @DisplayName("GET " + DOUGHNUT_CHART + " returns doughnut chart data for the dashboard")
    @WithMockUser(username = userName, roles = {"USER"})
    void doughnutChartShouldReturnData() throws Exception {
        when(entryService.getTotalPorPeriodo(any(Date.class), any(Date.class)))
                .thenReturn(genDoughnutData());
        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(DOUGHNUT_CHART)
                                .param("startDate", "2025-12-01")
                                .param("endDate", "2025-12-31")
                )
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    @DisplayName("GET " + DOUGHNUT_CHART + " returns doughnut chart data for the dashboard without date parameters")
    @WithMockUser(username = userName, roles = {"USER"})
    void doughnutChartShouldReturnDataWithoutDate() throws Exception {
        when(entryService.getTotalPorPeriodo(any(Date.class), any(Date.class)))
                .thenReturn(genDoughnutData());
        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(DOUGHNUT_CHART)
                )
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isNotEmpty());
    }

    private List<TotalLancamentoCategoriaRecord> genTableData() {
        TotalLancamentoCategoriaRecord record1 = new TotalLancamentoCategoriaRecord(BigDecimal.ONE, TipoLancamento.EXPENSE, Category.FOOD);
        TotalLancamentoCategoriaRecord record2 = new TotalLancamentoCategoriaRecord(BigDecimal.TEN, TipoLancamento.EXPENSE, Category.LEISURE);
        return List.of(record1, record2);
    }

    private List<TotalLancamentoRecord> genDoughnutData() {
        TotalLancamentoRecord record1 = new TotalLancamentoRecord(BigDecimal.ONE, TipoLancamento.EXPENSE);
        TotalLancamentoRecord record2 = new TotalLancamentoRecord(BigDecimal.TEN, TipoLancamento.EXPENSE);
        return List.of(record1, record2);
    }

}
