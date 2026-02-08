package br.com.home.lab.softwaretesting.controller;

import br.com.home.lab.softwaretesting.model.Category;
import br.com.home.lab.softwaretesting.model.TipoLancamento;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/entryInfo")
public class EntryInfoController {

    private static final Set<String> CATEGORIES =
            Arrays.stream(Category.values())
                    .map(Category::getNome)
                    .map(String::toLowerCase)
                    .collect(Collectors.toUnmodifiableSet());

    private static final Set<String> ENTRY_TYPES =
            Arrays.stream(TipoLancamento.values())
                    .map(TipoLancamento::getTipo)
                    .map(String::toLowerCase)
                    .collect(Collectors.toUnmodifiableSet());

    @GetMapping("/categories")
    public ResponseEntity<Set<String>> categories(){
        return ResponseEntity.ok(CATEGORIES);
    }

    @GetMapping("/entryTypes")
    public ResponseEntity<Set<String>> entryTypes(){
        return ResponseEntity.ok(ENTRY_TYPES);
    }
}
