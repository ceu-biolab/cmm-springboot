package com.example.myapp.service;

import com.example.myapp.api.CcsSearchRequest;
import com.example.myapp.model.CompoundCcsDTO;
import com.example.myapp.model.CcsRangeMatchesDTO;
import com.example.myapp.repository.CompoundCcsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CompoundCcsService {

    @Autowired
    private CompoundCcsRepository compoundCcsRepository;

    public List<CcsRangeMatchesDTO> findCompoundsByCcsRangesGrouped(List<CcsSearchRequest.CcsRange> ranges) {
        if (ranges == null || ranges.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<CcsRangeMatchesDTO> results = new ArrayList<>();
        
        for (CcsSearchRequest.CcsRange range : ranges) {
            // Create processed range with absolute tolerance value
            CcsSearchRequest.CcsRange processedRange = new CcsSearchRequest.CcsRange();
            processedRange.setValue(range.getValue());
            
            // Store original percentage for reference
            double tolerancePercentage = range.getTolerance();
            
            // Convert percentage tolerance to absolute value
            double absoluteTolerance = range.getValue() * tolerancePercentage / 100;
            processedRange.setTolerance(absoluteTolerance);
            
            // Query database for this specific range
            List<CompoundCcsDTO> matches = compoundCcsRepository.findCompoundsByCcsRange(processedRange);
            
            // Create result object with the range info and its matches
            CcsRangeMatchesDTO rangeMatches = new CcsRangeMatchesDTO(
                range.getValue(),
                absoluteTolerance,
                tolerancePercentage,
                matches
            );
            
            results.add(rangeMatches);
        }
        
        return results;
    }
}
