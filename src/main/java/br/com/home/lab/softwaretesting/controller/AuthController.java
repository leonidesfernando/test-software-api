package br.com.home.lab.softwaretesting.controller;


import br.com.home.lab.softwaretesting.controller.record.UserEmailRecord;
import br.com.home.lab.softwaretesting.controller.record.UserIDRecord;
import br.com.home.lab.softwaretesting.model.ERole;
import br.com.home.lab.softwaretesting.model.Role;
import br.com.home.lab.softwaretesting.model.User;
import br.com.home.lab.softwaretesting.payload.JwtResponse;
import br.com.home.lab.softwaretesting.payload.LoginRequest;
import br.com.home.lab.softwaretesting.payload.MessageResponse;
import br.com.home.lab.softwaretesting.payload.SignupRequest;
import br.com.home.lab.softwaretesting.repository.RoleRepository;
import br.com.home.lab.softwaretesting.repository.UserRepository;
import br.com.home.lab.softwaretesting.security.jwt.JwtUtils;
import br.com.home.lab.softwaretesting.service.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static br.com.home.lab.softwaretesting.util.Constantes.BEARER;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    AuthenticationManager authenticationManager;

    JwtUtils jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager, JwtUtils jwtUtil, PasswordEncoder passwordEncoder){
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/signin")
    public ResponseEntity<JwtResponse> signin(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtil.generateJwtToken(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        JwtResponse res = new JwtResponse(jwt, BEARER, userDetails.getId(), userDetails.getUsername(),
                userDetails.getEmail(), roles);
        return ResponseEntity.ok(res);
    }


    @PostMapping("/signout")
    public ResponseEntity<MessageResponse> logoutUser() {
        //TODO: receive the user to be validate if is the logged user and then logout
        ResponseCookie cookie = jwtUtil.getCleanJwtCookie();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new MessageResponse("user.signed.out"));
    }

    @PostMapping("/signup")
    public ResponseEntity<MessageResponse> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (Boolean.TRUE.equals(userRepository.existsByUsername(signUpRequest.username()))) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("username.taken"));
        }

        if (Boolean.TRUE.equals(userRepository.existsByEmail(signUpRequest.email()))) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("email.taken"));
        }

        // Create new user's account
        User user = new User(signUpRequest.name(), signUpRequest.username(),
                signUpRequest.email(),
                passwordEncoder.encode(signUpRequest.password()));

        Set<String> strRoles = signUpRequest.roles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role user is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role admin is not found."));
                        roles.add(adminRole);

                        break;
                    case "mod":
                        Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
                                .orElseThrow(() -> new RuntimeException("Error: Role moderator is not found."));
                        roles.add(modRole);

                        break;
                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role user is not found."));
                        roles.add(userRole);
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponse("user.registered.successfully"));
    }

    @PostMapping("/findUserByAdmin")
    public ResponseEntity<MessageResponse> findUserByAdmin(@RequestBody UserEmailRecord userToBeFound){
        boolean userExists = userRepository.existsByEmail(userToBeFound.email());
        if(userExists){
            return ResponseEntity.ok(new MessageResponse("user.found"));
        }else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("user.not.found"));
        }
    }

    @Deprecated
    @DeleteMapping("/removeUser")
    public ResponseEntity<MessageResponse> removeUser(@RequestBody UserIDRecord userTobeRemoved){
        var loggedUser = getLoggedUser();
        if(!loggedUser.getId().equals(userTobeRemoved.id())){
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("user.to.be.removed.does.not.match.logged.in.user", null));
        }
        try {
            userRepository.deleteById(userTobeRemoved.id());
            jwtUtil.getCleanJwtCookie();
            return ResponseEntity.ok().body(new MessageResponse("user.removed", userTobeRemoved.id()));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("user.could.not.be.removed", userTobeRemoved.id()));
        }
    }

    public static UserDetailsImpl getLoggedUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (UserDetailsImpl) authentication.getPrincipal();
    }

    public static ResponseEntity<MessageResponse> isLoggedUserForbidden(Long userId){
        if(!userId.equals(getLoggedUser().getId())){
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("entry.does.not.belong.to.logged.in.user", null));
        }
        return null;
    }

}