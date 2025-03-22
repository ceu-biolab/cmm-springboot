package com.example.myapp.service;

import com.example.myapp.model.Databases;
import com.example.myapp.model.MetaboliteType;
import com.example.myapp.model.msFeature.MSFeature;
import com.example.myapp.repository.CompoundRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.myapp.model.IonizationMode;
import com.example.myapp.model.ToleranceMode;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CompoundService {
    private static final Logger logger = LoggerFactory.getLogger(CompoundService.class);

    @Autowired
    private CompoundRepository compoundRepository;

    public Set<MSFeature> findCompoundsByMzRanges(Double mz, ToleranceMode toleranceMode, Double tolerance,
                                                  IonizationMode ionizationMode, Set<String> adductsString,
                                                  Set<Databases> databases, MetaboliteType metaboliteType) {
        logger.info("Received request to find compounds with mz: {}", mz);

        try {
            Set<MSFeature> results = compoundRepository.annotateMSFeature(mz, toleranceMode, tolerance, ionizationMode, adductsString, databases, metaboliteType);
            logger.info("Number of MSFeatures found: {}", results.size());
            return results;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error in CompoundService: ", e);
            throw new RuntimeException("Error annotating MS features", e);
        }
    }
}
