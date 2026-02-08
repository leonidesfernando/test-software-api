package br.com.home.lab.softwaretesting.controller;

import br.com.home.lab.softwaretesting.security.jwt.AuthTokenFilter;
import br.com.home.lab.softwaretesting.security.jwt.JwtUtils;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = CheckController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = AuthTokenFilter.class
        )
)
@AutoConfigureMockMvc(addFilters = false)
class CheckControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtUtils jwtUtils;

    private static final String API_CHECK_PROFILE = "/api/check/profile";
    private static final String userName = "testuser";

    @Test
    @DisplayName("GET " + API_CHECK_PROFILE + " returns the username of the authenticated user")
    @WithMockUser(username = userName, roles = {"USER"})
    void profileShouldReturnUsername() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(API_CHECK_PROFILE))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"username\":\"" + userName + "\"}"));
    }

    @Test
    @DisplayName("GET " + API_CHECK_PROFILE + " returns 404 Not Found when no user is authenticated")
    void profileShouldNotReturnUsername() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(API_CHECK_PROFILE))
                .andExpect(status().isNotFound());
    }
}