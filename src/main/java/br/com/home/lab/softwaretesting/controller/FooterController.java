package br.com.home.lab.softwaretesting.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@Log4j2
@RestController
@RequestMapping("/api/footer")
public class FooterController {

    @Value("${env}")
    private String env;

    @Value("${db.name}")
    private String dbName;

    @Value("${db.vendor}")
    private String dbVendor;

    @GetMapping("/env")
    public ResponseEntity<String> env() {
        return ResponseEntity.ok(env);
    }

    @GetMapping("/dbName")
    public ResponseEntity<String> dbName() {
        return ResponseEntity.ok(dbName);
    }

    @GetMapping("/dbVendor")
    public ResponseEntity<String> dbVendor(){
        return ResponseEntity.ok(dbVendor);
    }
}
