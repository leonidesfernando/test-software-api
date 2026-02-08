package br.com.home.lab.softwaretesting.controller;


import br.com.home.lab.softwaretesting.controller.record.UserEmailRecord;
import br.com.home.lab.softwaretesting.exception.RoleNotFoundException;
import br.com.home.lab.softwaretesting.exception.UserAlreadyExistsException;
import br.com.home.lab.softwaretesting.model.ERole;
import br.com.home.lab.softwaretesting.model.Role;
import br.com.home.lab.softwaretesting.model.User;
import br.com.home.lab.softwaretesting.payload.LoginRequest;
import br.com.home.lab.softwaretesting.payload.SignupRequest;
import br.com.home.lab.softwaretesting.repository.RoleRepository;
import br.com.home.lab.softwaretesting.repository.UserRepository;
import br.com.home.lab.softwaretesting.security.jwt.AuthTokenFilter;
import br.com.home.lab.softwaretesting.security.jwt.JwtUtils;
import br.com.home.lab.softwaretesting.service.RegisterUserService;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static br.com.home.lab.softwaretesting.controller.TestAuthHelpers.buildSignupRequest;
import static br.com.home.lab.softwaretesting.controller.TestAuthHelpers.givenEmailExists;
import static br.com.home.lab.softwaretesting.controller.TestAuthHelpers.givenUsernameExists;
import static br.com.home.lab.softwaretesting.controller.TestAuthHelpers.setUserRoleUser;
import static br.com.home.lab.softwaretesting.controller.TestAuthHelpers.stubPasswordEncoder;
import static br.com.home.lab.softwaretesting.controller.TestAuthHelpers.stubRoleRepositoryForUser;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AuthController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = AuthTokenFilter.class
        )
)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; // for JSON serialization

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RoleRepository roleRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private RegisterUserService registerUserService;

    private static final String API_AUTH = "/api/auth";

    private final User user = new User("John Doe", "john", "john@example.com", "encoded");

    @Test
    @DisplayName("POST " + API_AUTH + "/signin successful login returns JWT response")
    void signin_successfulLogin_returnsJwtResponse() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest(user.getUsername(), user.getPassword());

        user.setRoles(Set.of(new Role(ERole.ROLE_USER)));
        UserDetails userDetails = UserDetailsImpl.build(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn("fake-jwt-token");

        // Act & Assert
        mockMvc.perform(post(API_AUTH + "/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-jwt-token"))
                .andExpect(jsonPath("$.username").value(user.getUsername()))
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));
    }

    @Test
    @DisplayName("POST " + API_AUTH + "/signup successful registration registers new user")
    void signup_newUser_registersSuccessfully() throws Exception {
        // Arrange
        setUserRoleUser(user);
        SignupRequest signupRequest = buildSignupRequest(user);

        givenUsernameExists(userRepository, user.getUsername(), false);
        givenEmailExists(userRepository, user.getEmail(), false);
        stubRoleRepositoryForUser(roleRepository, user);
        stubPasswordEncoder(passwordEncoder, user.getPassword(), "encoded-password");

        // Act & Assert
        mockMvc.perform(post(API_AUTH + "/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("user.registered.successfully"));

        verify(registerUserService).registerUser(signupRequest);
    }

    @Test
    @DisplayName("POST " + API_AUTH + "/signup with taken username returns bad request")
    void signup_usernameTaken_returnsBadRequest() throws Exception {
        // Arrange
        SignupRequest signupRequest = buildSignupRequest(user);

        doThrow(new UserAlreadyExistsException("username.taken"))
                .when(registerUserService)
                .registerUser(any(SignupRequest.class));

        // Act & Assert
        mockMvc.perform(post(API_AUTH + "/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("username.taken"));
    }

    @Test
    @DisplayName("POST " + API_AUTH + "/signup with taken email returns bad request")
    void signup_usernameEmailTaken_returnsBadRequest() throws Exception {
        // Arrange
        setUserRoleUser(user);
        SignupRequest signupRequest = buildSignupRequest(user);
        doThrow(new UserAlreadyExistsException("email.taken"))
                .when(registerUserService)
                .registerUser(any(SignupRequest.class));

        // Act & Assert
        mockMvc.perform(post(API_AUTH + "/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("email.taken"));
    }

    @Test
    @DisplayName("POST " + API_AUTH + "/signup with taken email returns bad request")
    void signup_roleNotFound_returnsBadRequest() throws Exception {
        // Arrange

        SignupRequest signupRequest = buildSignupRequest(user);
        doThrow(new RoleNotFoundException("role.notfound"))
                .when(registerUserService)
                .registerUser(any(SignupRequest.class));

        // Act & Assert
        mockMvc.perform(post(API_AUTH + "/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("role.notfound"));
    }


    @Test
    @WithMockUser
    @DisplayName("POST " + API_AUTH + "/signout clears JWT cookie and returns signout message")
    void signout_clearsJwtCookie() throws Exception {
        ResponseCookie cleanCookie = ResponseCookie.from("jwt", "")
                .path("/api")
                .maxAge(0)
                .httpOnly(true)
                .build();

        when(jwtUtils.getCleanJwtCookie()).thenReturn(cleanCookie);
        mockMvc.perform(post(API_AUTH + "/signout"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("jwt=;")))
                .andExpect(jsonPath("$.message").value("user.signed.out"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST " + API_AUTH + "/findUserByAdmin return user found")
    void findUserByAdmin_userFound() throws Exception {

        UserEmailRecord request = new UserEmailRecord(user.getEmail());
        // Arrange
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post(API_AUTH + "/findUserByAdmin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))) // ← Send JSON body
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("user.found"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/auth/findUserByAdmin returns 404 when user does not exist")
    void findUserByAdmin_userNotFound() throws Exception {
        // Arrange
        UserEmailRecord request = new UserEmailRecord(user.getEmail());

        when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post(API_AUTH + "/findUserByAdmin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("user.not.found"));
    }
}
