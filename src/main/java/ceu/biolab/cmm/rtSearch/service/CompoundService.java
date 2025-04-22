package ceu.biolab.cmm.rtSearch.service;

import ceu.biolab.cmm.rtSearch.repository.CompoundRepository;

import ceu.biolab.cmm.shared.domain.Database;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.MetaboliteType;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;
import ceu.biolab.cmm.shared.domain.msFeature.AnnotatedFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class CompoundService {

    @Autowired
    private CompoundRepository compoundRepository;


    public List<AnnotatedFeature> findCompoundsByMz(Double mz, MzToleranceMode mzToleranceMode, Double tolerance,
                                            IonizationMode ionizationMode, Set<String> adductsString,
                                            Set<Database> databases, MetaboliteType metaboliteType) {

        try {
            List<AnnotatedFeature> results = compoundRepository.annotateMSFeature(mz, mzToleranceMode, tolerance, ionizationMode, adductsString, databases, metaboliteType);
            return results;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error annotating MS features", e);
        }
    }
}
