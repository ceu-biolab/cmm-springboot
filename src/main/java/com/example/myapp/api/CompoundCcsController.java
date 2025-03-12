package com.example.myapp.api;

import com.example.myapp.model.ccsMatcher.CcsRangeMatchesDTO;
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
    public List<CcsRangeMatchesDTO> getCompoundsByCcsTolerance(@RequestBody CcsSearchRequest request) {
        if (request.getRanges() == null || request.getRanges().isEmpty()) {
            return new ArrayList<>();
        }
        
        return compoundCcsService.findCompoundsByCcsRangesGrouped(request.getRanges());
    }
}
