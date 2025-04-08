package ceu.biolab.cmm.rtSearch.service;

import ceu.biolab.cmm.rtSearch.model.msFeature.MSFeature;

import ceu.biolab.cmm.rtSearch.repository.CompoundRepository;

import ceu.biolab.cmm.shared.domain.Database;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.MetaboliteType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ceu.biolab.cmm.rtSearch.model.ToleranceMode;

import java.util.Set;

@Service
public class CompoundService {

    @Autowired
    private CompoundRepository compoundRepository;

    public Set<MSFeature> findCompoundsByMz(Double mz, ToleranceMode toleranceMode, Double tolerance,
                                            IonizationMode ionizationMode, Set<String> adductsString,
                                            Set<Database> databases, MetaboliteType metaboliteType) {

        try {
            Set<MSFeature> results = compoundRepository.annotateMSFeature(mz, toleranceMode, tolerance, ionizationMode, adductsString, databases, metaboliteType);
            return results;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error annotating MS features", e);
        }
    }
}
