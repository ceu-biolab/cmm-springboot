package ceu.biolab.cmm.rtSearch.service;

import ceu.biolab.cmm.rtSearch.model.Databases;
import ceu.biolab.cmm.rtSearch.model.MetaboliteType;
import ceu.biolab.cmm.rtSearch.model.msFeature.MSFeature;

import ceu.biolab.cmm.rtSearch.repository.CompoundRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ceu.biolab.cmm.rtSearch.model.IonizationMode;
import ceu.biolab.cmm.rtSearch.model.ToleranceMode;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CompoundService {

    @Autowired
    private CompoundRepository compoundRepository;

    public Set<MSFeature> findCompoundsByMzRanges(Double mz, ToleranceMode toleranceMode, Double tolerance,
                                                  IonizationMode ionizationMode, Set<String> adductsString,
                                                  Set<Databases> databases, MetaboliteType metaboliteType) {

        try {
            Set<MSFeature> results = compoundRepository.annotateMSFeature(mz, toleranceMode, tolerance, ionizationMode, adductsString, databases, metaboliteType);
            return results;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error annotating MS features", e);
        }
    }
}
