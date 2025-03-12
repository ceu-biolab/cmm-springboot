package com.example.myapp.service;

import com.example.myapp.api.CcsSearchRequest;
import com.example.myapp.model.CompoundCcsDTO;
import com.example.myapp.repository.CompoundCcsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CompoundCcsService {

    @Autowired
    private CompoundCcsRepository compoundCcsRepository;

    public List<CompoundCcsDTO> findCompoundsByCcsRanges(List<CcsSearchRequest.CcsRange> ranges) {
        if (ranges == null || ranges.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Process each range to convert the tolerance from percentage to absolute value
        List<CcsSearchRequest.CcsRange> processedRanges = new ArrayList<>();
        for (CcsSearchRequest.CcsRange range : ranges) {
            CcsSearchRequest.CcsRange processedRange = new CcsSearchRequest.CcsRange();
            processedRange.setValue(range.getValue());
            // Convert percentage tolerance to absolute value
            processedRange.setTolerance(range.getValue() * range.getTolerance() / 100);
            processedRanges.add(processedRange);
        }
        
        return compoundCcsRepository.findCompoundsByMultipleCcsRanges(processedRanges);
    }
}
