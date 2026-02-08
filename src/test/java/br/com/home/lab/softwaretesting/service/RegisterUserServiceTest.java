package br.com.home.lab.softwaretesting.service;

import br.com.home.lab.softwaretesting.exception.RoleNotFoundException;
import br.com.home.lab.softwaretesting.exception.UserAlreadyExistsException;
import br.com.home.lab.softwaretesting.model.ERole;
import br.com.home.lab.softwaretesting.model.Role;
import br.com.home.lab.softwaretesting.payload.SignupRequest;
import br.com.home.lab.softwaretesting.repository.RoleRepository;
import br.com.home.lab.softwaretesting.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegisterUserService registerUserService;

    @Test
    void registerUser_whenUsernameExists_throwsException() {
        // Arrange
        SignupRequest request = buildSignupRequest();
        when(userRepository.existsByUsername("john")).thenReturn(true);

        // Act + Assert
        assertThrows(UserAlreadyExistsException.class,
                () -> registerUserService.registerUser(request));
    }

    @Test
    void registerUser_whenRoleNotFound_throwsException() {
        // Arrange
        SignupRequest request = buildSignupRequest();
        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(userRepository.existsByEmail(request.email())).thenReturn(false);

        // Act + Assert
        assertThrows(RoleNotFoundException.class,
                () -> registerUserService.registerUser(request));
    }

    @Test
    void registerUser_whenEmailTaken_throwsException() {
        // Arrange
        SignupRequest request = buildSignupRequest();
        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        // Act + Assert
        assertThrows(UserAlreadyExistsException.class,
                () -> registerUserService.registerUser(request));
    }

    @Test
    void registerUser_withoutRole_assignsDefaultUserRole() {
        // Arrange
        SignupRequest request = buildSignupRequestWithoutRole(); // roles = null or empty
        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        Role userRole = new Role(ERole.ROLE_USER);
        when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.of(userRole));

        // Act
        registerUserService.registerUser(request);

        // Assert - user is saved with exactly one role: ROLE_USER
        verify(userRepository).save(argThat(savedUser ->
                savedUser.getUsername().equals(request.username()) &&
                        savedUser.getEmail().equals(request.email()) &&
                        savedUser.getRoles() != null &&
                        savedUser.getRoles().size() == 1 &&
                        savedUser.getRoles().iterator().next().getName() == ERole.ROLE_USER
        ));
    }

    @Test
    void registerUser_withoutRole_andDefaultRoleMissing_throwsRoleNotFoundException() {
        // Arrange
        SignupRequest request = buildSignupRequestWithoutRole();
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RoleNotFoundException.class, () -> registerUserService.registerUser(request));
    }

    @Test
    void registerUser_withMultipleRoles_assignsCorrectRoles() {
        // Arrange
        Set<String> requestedRoles = Set.of("admin", "mod"); // lowercase as sent from frontend
        SignupRequest request = new SignupRequest(
                "Jane Doe",
                "jane",
                "jane@example.com",
                "password123",
                requestedRoles
        );

        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        Role adminRole = new Role(ERole.ROLE_ADMIN);
        Role modRole = new Role(ERole.ROLE_MODERATOR);
        when(roleRepository.findByName(ERole.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));
        when(roleRepository.findByName(ERole.ROLE_MODERATOR)).thenReturn(Optional.of(modRole));

        // Act
        registerUserService.registerUser(request);

        // Assert: save called with user having both roles
        verify(userRepository).save(argThat(savedUser ->
                savedUser.getUsername().equals(request.username()) &&
                        savedUser.getRoles() != null &&
                        savedUser.getRoles().size() == 2 &&
                        savedUser.getRoles().stream()
                                .map(Role::getName)
                                .collect(Collectors.toSet())
                                .containsAll(Set.of(ERole.ROLE_ADMIN, ERole.ROLE_MODERATOR))
        ));
    }

    private SignupRequest buildSignupRequest() {
        return new SignupRequest(
                "John Doe",
                "john",
                "john@example.com",
                "password",
                Set.of("user")
        );
    }

    private SignupRequest buildSignupRequestWithoutRole() {
        return new SignupRequest(
                "John Doe",
                "john",
                "john@example.com",
                "password",
                Collections.emptySet()
        );
    }
}
