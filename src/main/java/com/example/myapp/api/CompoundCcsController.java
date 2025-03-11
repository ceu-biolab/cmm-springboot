package com.example.myapp.api;

import com.example.myapp.model.CompoundCcsDTO;
import com.example.myapp.repository.CompoundCcsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CompoundCcsController {

    @Autowired
    private CompoundCcsRepository compoundCcsRepository;

    @GetMapping("/ccs")
    public List<CompoundCcsDTO> getCompoundsByCcsTolerance(
            @RequestParam("value") double ccsValue,
            @RequestParam("tolerance") double tolerancePercentage) {
        // Calculate the lower and upper bounds using the tolerance percentage.
        double lowerBound = ccsValue * (1 - tolerancePercentage / 100);
        double upperBound = ccsValue * (1 + tolerancePercentage / 100);
        return compoundCcsRepository.findCompoundsByCcsRange(lowerBound, upperBound);
    }
}
