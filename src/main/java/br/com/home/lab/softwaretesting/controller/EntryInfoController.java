package br.com.home.lab.softwaretesting.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/entryInfo")
public class EntryInfoController {

    private Set<String> categories = Set.of("food","wage","leisure","phone.internet",
            "loan","investments","clothing", "other");

    private Set<String> entryTypes = Set.of("income","expense","transf");

    @GetMapping("/categories")
    public ResponseEntity<Set<String>> categories(){
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/entryTypes")
    public ResponseEntity<Set<String>> entryTypes(){
        return ResponseEntity.ok(entryTypes);
    }
}
