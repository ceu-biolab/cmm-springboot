package com.example.myapp.api;

import com.example.myapp.model.CompoundCcsDTO;
import com.example.myapp.service.CompoundCcsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/api")
public class CompoundCcsController {

    @Autowired
    private CompoundCcsService compoundCcsService;

    @PostMapping("/ccs")
    public List<CompoundCcsDTO> getCompoundsByCcsTolerance(@RequestBody CcsSearchRequest request) {
        if (request.getRanges() == null || request.getRanges().isEmpty()) {
            return new ArrayList<>();
        }
        
        return compoundCcsService.findCompoundsByCcsRanges(request.getRanges());
    }
}
