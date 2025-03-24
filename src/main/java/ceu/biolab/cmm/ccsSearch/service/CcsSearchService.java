package ceu.biolab.cmm.ccsSearch.service;

import ceu.biolab.cmm.ccsSearch.dto.CcsFeatureQuery;
import ceu.biolab.cmm.ccsSearch.dto.CcsQueryResponse;
import ceu.biolab.cmm.ccsSearch.dto.CcsSearchRequest;
import ceu.biolab.cmm.ccsSearch.dto.CcsSearchResponse;
import ceu.biolab.cmm.ccsSearch.repository.CcsSearchRepository;
import ceu.biolab.cmm.ccsSearch.domain.CcsToleranceMode;
import ceu.biolab.cmm.ccsSearch.domain.IMFeature;
import ceu.biolab.cmm.ccsSearch.domain.AnnotationsByAdduct;
import ceu.biolab.cmm.ccsSearch.domain.IMMSFeature;
import ceu.biolab.cmm.shared.domain.BufferGas;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;
import ceu.biolab.cmm.shared.domain.Pathway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

@Service
public class CcsSearchService {

    @Autowired
    private CcsSearchRepository ccsSearchRepository;

    public CcsSearchResponse search(CcsSearchRequest request) {
        if (request.getCcsValues().size() != request.getMzValues().size()) {
            throw new IllegalArgumentException("Number of CCS values and m/z values must be equal.");
        }
        int nFeatures = request.getCcsValues().size();
        MzToleranceMode mzToleranceMode = request.getMzToleranceMode();
        CcsToleranceMode ccsToleranceMode = request.getCcsToleranceMode();
        BufferGas bufferGas = request.getBufferGas();

        double absoluteCcsTolerance;
        if (ccsToleranceMode == CcsToleranceMode.PERCENTAGE) {
            absoluteCcsTolerance = request.getCcsTolerance() * 0.01;
        } else if (ccsToleranceMode == CcsToleranceMode.ABSOLUTE) {
            absoluteCcsTolerance = request.getCcsTolerance();
        } else {
            throw new IllegalArgumentException("Invalid tolerance mode: " + request.getCcsToleranceMode());
        }

        CcsSearchResponse response = new CcsSearchResponse();
        for (int i = 0; i < nFeatures; i++) 
        {

            double mz = request.getMzValues().get(i);
            double ccs = request.getCcsValues().get(i);
            IMFeature imFeature = new IMFeature(mz, ccs);

            // TODO: Dummy adduct. Replace with actual adducts from request. Also add IonMode check?
            ArrayList<String> adducts = new ArrayList<>(java.util.Arrays.asList("M+H"));
            ArrayList<Double> adductIonMassDifferences = new ArrayList<>(java.util.Arrays.asList(1.007276));
            for (int j = 0; j < adducts.size(); j++) {
                String adduct = adducts.get(j);
                double ionMassDifference = adductIonMassDifferences.get(j);

                double ccsDifference = ccs * absoluteCcsTolerance;
                double ccsLower = ccs - ccsDifference;
                double ccsUpper = ccs + ccsDifference;

                double neutralMass = mz - ionMassDifference;
                double mzDifference;
                if (mzToleranceMode == MzToleranceMode.PPM) {
                    mzDifference = mz * request.getMzTolerance() * 0.000001;
                } else if (mzToleranceMode == MzToleranceMode.MDA) {
                    mzDifference = request.getMzTolerance() * 0.001;
                } else {
                    throw new IllegalArgumentException("Invalid tolerance mode: " + request.getMzToleranceMode());
                }
                double massLower = neutralMass - mzDifference;
                double massUpper = neutralMass + mzDifference;

                CcsFeatureQuery queryData = new CcsFeatureQuery(ccsLower, ccsUpper, massLower, massUpper, bufferGas.toString(), adduct);
                try {
                    List<CcsQueryResponse> queryResults = ccsSearchRepository.findMatchingCompounds(queryData);
                    // QueryResults may have duplicate results where the same compound is found with different pathways.
                    // We need to merge these results.
                    List<IMMSFeature> compounds = new ArrayList<>();
                    for (CcsQueryResponse queryResult : queryResults) {
                        Pathway pathway = new Pathway(queryResult.getPathwayId(), queryResult.getPathwayName(), queryResult.getPathwayMap());

                        boolean found = false;
                        for (IMMSFeature compound : compounds) {
                            if (compound.getCompoundId() == queryResult.getCompoundId()) {
                                compound.addPathway(pathway);
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            // TODO ugly
                            IMMSFeature compound = new IMMSFeature(queryResult.getCompoundId(), queryResult.getCompoundName(), queryResult.getMonoisotopicMass(), queryResult.getDbCcs(), queryResult.getFormula(), queryResult.getFormulaType(), queryResult.getCompoundType(), queryResult.getLogP(), new ArrayList<>());
                            compound.addPathway(pathway);
                            compounds.add(compound);
                        }
                    }
                    AnnotationsByAdduct annotations = new AnnotationsByAdduct(adduct, compounds);
                    imFeature.addAnnotations(annotations);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            response.addImFeature(imFeature);
        }

        return response;
    }
}
