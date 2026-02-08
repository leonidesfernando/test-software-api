package br.com.home.lab.softwaretesting.service;

import br.com.home.lab.softwaretesting.exception.RoleNotFoundException;
import br.com.home.lab.softwaretesting.exception.UserAlreadyExistsException;
import br.com.home.lab.softwaretesting.model.ERole;
import br.com.home.lab.softwaretesting.model.Role;
import br.com.home.lab.softwaretesting.model.User;
import br.com.home.lab.softwaretesting.payload.SignupRequest;
import br.com.home.lab.softwaretesting.repository.RoleRepository;
import br.com.home.lab.softwaretesting.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegisterUserService {

    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final RoleRepository roleRepository;
    @Autowired
    private final PasswordEncoder passwordEncoder;

    public void registerUser(SignupRequest request) {

        if (userRepository.existsByUsername(request.username())) {
            throw new UserAlreadyExistsException("username.taken");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("email.taken");
        }

        User user = new User(
                request.name(),
                request.username(),
                request.email(),
                passwordEncoder.encode(request.password())
        );

        user.setRoles(resolveRoles(request.roles()));
        userRepository.save(user);
    }

    protected Set<Role> resolveRoles(Set<String> roles) {

        if (roles == null || roles.isEmpty()) {
            return Set.of(getRole(ERole.ROLE_USER));
        }

        return roles.stream()
                .map(this::mapToRole)
                .collect(Collectors.toSet());
    }

    protected Role mapToRole(String role) {
        return switch (role.toLowerCase()) {
            case "admin" -> getRole(ERole.ROLE_ADMIN);
            case "mod" -> getRole(ERole.ROLE_MODERATOR);
            default -> getRole(ERole.ROLE_USER);
        };
    }

    protected Role getRole(ERole role) {
        return roleRepository.findByName(role)
                .orElseThrow(() -> new RoleNotFoundException(role.name()));
    }
}
