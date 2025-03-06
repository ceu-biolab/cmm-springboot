package com.example.myapp.api;

import com.example.myapp.repository.CompoundRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class CompoundController {

    @Autowired
    private CompoundRepository compoundRepository;

    @GetMapping("/compound/{compoundName}")
    public ResponseEntity<Double> getMass(@PathVariable String compoundName) {
        Double mass = compoundRepository.findMassByCompoundName(compoundName);
        if (mass != null) {
            return ResponseEntity.ok(mass);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
