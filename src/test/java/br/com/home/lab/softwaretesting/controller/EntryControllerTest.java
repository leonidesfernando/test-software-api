package br.com.home.lab.softwaretesting.controller;

import br.com.home.lab.softwaretesting.controller.record.EntryRecord;
import br.com.home.lab.softwaretesting.controller.record.ResultRecord;
import br.com.home.lab.softwaretesting.excel.exporter.FastexcelExporter;
import br.com.home.lab.softwaretesting.model.Lancamento;
import br.com.home.lab.softwaretesting.model.User;
import br.com.home.lab.softwaretesting.repository.UserRepository;
import br.com.home.lab.softwaretesting.security.jwt.AuthTokenFilter;
import br.com.home.lab.softwaretesting.service.EntryService;
import br.com.home.lab.softwaretesting.service.UserDetailsImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.stereotype.Service;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(EntryControllerTest.TestUserDetailsService.class)
@WebMvcTest(
        controllers = EntryController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = AuthTokenFilter.class
        )
)
@AutoConfigureMockMvc(addFilters = false)
class EntryControllerTest {

    private static final String API_ENTRIES_ADD = "/api/entries/add";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EntryService entryService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private FastexcelExporter<ResultRecord> fastExcelExporter;

    @Test
    @WithUserDetails(value = "testuser", userDetailsServiceBeanName = "testUserDetailsService")
    @DisplayName("POST /api/entries/add - success: entry added for logged user")
    void addNew_success() throws Exception {
        // Arrange
        long loggedUserId = 1L;
        long entryIdAfterSave = 100L;

        EntryRecord entryRecord = buildEntryRecord(loggedUserId);

        mockUserAndSavedLancamento(entryRecord, entryIdAfterSave);

        // Act & Assert
        performAdd(entryRecord)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("entry.added"))
                .andExpect(jsonPath("$.id").value(entryIdAfterSave));

        verify(entryService).save(any(Lancamento.class));
    }

    @Test
    @WithUserDetails(value = "testuser", userDetailsServiceBeanName = "testUserDetailsService")
    @DisplayName("POST /api/entries/add - validation fails → 400")
    void addNew_validationFails_returnsBadRequest() throws Exception {
        // Arrange: invalid data (e.g., empty description or invalid value)
        long loggedUserId = 1L;
        EntryRecord invalidRecord = buildInvalidEntryRecord(loggedUserId);

        // Act & Assert
        performAdd(invalidRecord)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.message").value("Description must be informed"));

        // Ensure save was NOT called
        verify(entryService, never()).save(any());
    }


    @Test
    @DisplayName("POST /api/entries/add - user tries to add entry for another user → 403 Forbidden")
    @WithUserDetails(value = "testuser", userDetailsServiceBeanName = "testUserDetailsService")
    void addNew_differentUser_returnsForbidden() throws Exception {
        // Arrange
        long otherUserId = 999L;

        EntryRecord entryRecord = buildEntryRecord(otherUserId);

        // Act & Assert
        performAdd(entryRecord)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("entry.does.not.belong.to.logged.in.user"));

        // Save should NOT be called
        verify(entryService, never()).save(any());
    }

    // Helper methods to reduce duplication and provide EntryRecord data
    private EntryRecord buildEntryRecord(long userId) {
        return new EntryRecord(
                0,
                "Supermaket purchase",
                "150.00",
                "2025-12-29",
                "EXPENSE",
                "FOOD",
                userId
        );
    }

    private EntryRecord buildInvalidEntryRecord(long userId) {
        return new EntryRecord(
                0,
                "",
                "150.00",
                "2025-12-29",
                "EXPENSE",
                "FOOD",
                userId
        );
    }

    private void mockUserAndSavedLancamento(EntryRecord entryRecord, long savedId) {
        Lancamento savedLancamento = entryRecord.build();
        savedLancamento.setId(savedId);
        when(userRepository.getById(entryRecord.userId()))
                .thenReturn(new User());

        when(entryService.save(any(Lancamento.class)))
                .thenReturn(savedLancamento);
    }

    private String asJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    private ResultActions performAdd(EntryRecord record) throws Exception {
        return mockMvc.perform(post(API_ENTRIES_ADD)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJson(record)));
    }

    @Service("testUserDetailsService")
    static class TestUserDetailsService implements UserDetailsService {

        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
            if ("testuser".equals(username)) {
                return new UserDetailsImpl(1L,
                        "Test User",
                        "testuser",
                        "test@example.com",
                        "password",
                        List.of((GrantedAuthority) () -> "ROLE_USER"));
            }
            throw new UsernameNotFoundException("User not found: " + username);
        }
    }
}
