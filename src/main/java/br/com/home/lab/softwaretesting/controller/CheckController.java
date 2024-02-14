package br.com.home.lab.softwaretesting.controller;

import br.com.home.lab.softwaretesting.service.UserDetailsImpl;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/check")
public class CheckController {

    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<Object> profile() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        try {
            //TODO: Create a interface to return only the username, at the moment we just need it
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            log.info("username: {}", userDetails.getUsername());
            return ResponseEntity.ok().body(userDetails);
        }catch (Exception e){
            return ResponseEntity.notFound().build();
        }
    }
}
