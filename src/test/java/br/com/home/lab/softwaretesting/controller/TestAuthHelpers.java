package br.com.home.lab.softwaretesting.controller;

import br.com.home.lab.softwaretesting.model.ERole;
import br.com.home.lab.softwaretesting.model.Role;
import br.com.home.lab.softwaretesting.model.User;
import br.com.home.lab.softwaretesting.payload.SignupRequest;
import br.com.home.lab.softwaretesting.repository.RoleRepository;
import br.com.home.lab.softwaretesting.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mockito.Mockito.when;

/**
 * Reusable helpers for Auth controller tests.
 * Keep these methods simple and static so they can be reused across test classes.
 */
public final class TestAuthHelpers {

    private TestAuthHelpers() {}

    public static void setUserRoleUser(User user) {
        user.setRoles(Set.of(new Role(ERole.ROLE_USER)));
    }

    public static SignupRequest buildSignupRequest(User user) {
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            setUserRoleUser(user);
        }
        return new SignupRequest(
                user.getName(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toSet())
        );
    }

    public static void stubRoleRepositoryForUser(RoleRepository roleRepository, User user) {
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            setUserRoleUser(user);
        }
        when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.of(user.getRoles().iterator().next()));
    }

    public static void stubNullRoleRepositoryForUser(RoleRepository roleRepository, User user) {
        user.setRoles(null);
        when(roleRepository.findByName(ERole.ROLE_USER)).thenThrow(new RuntimeException("Error: Role user is not found."));
    }

    public static void givenUsernameExists(UserRepository userRepository, String username, boolean exists) {
        when(userRepository.existsByUsername(username)).thenReturn(exists);
    }

    public static void givenEmailExists(UserRepository userRepository, String email, boolean exists) {
        when(userRepository.existsByEmail(email)).thenReturn(exists);
    }

    public static void stubPasswordEncoder(PasswordEncoder passwordEncoder, String rawPassword, String encoded) {
        when(passwordEncoder.encode(rawPassword)).thenReturn(encoded);
    }
}

