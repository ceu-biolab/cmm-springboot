package ceu.biolab.cmm.gcmsSearch.service;

import ceu.biolab.cmm.gcmsSearch.domain.*;
import ceu.biolab.cmm.gcmsSearch.dto.GCMSFeatureQueryDTO;
import ceu.biolab.cmm.gcmsSearch.dto.GCMSQueryResponseDTO;
import ceu.biolab.cmm.gcmsSearch.dto.GCMSSearchRequestDTO;
import ceu.biolab.cmm.gcmsSearch.dto.GCMSSearchResponseDTO;
import ceu.biolab.cmm.gcmsSearch.repository.GCMSSearchRepository;

import ceu.biolab.cmm.shared.domain.msFeature.Spectrum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class GCMSSearchService {

    @Autowired
    private GCMSSearchRepository gcmsSearchRepository;

    public GCMSSearchResponseDTO search(GCMSSearchRequestDTO request) {

        Spectrum gcmsSpectrumExperimental = request.getGcmsSpectrumExperimental();

        ColumnType columnType = request.getColumnType();
        DerivatizationMethod derivatizationMethod = request.getDerivatizationMethod();
        double RI = request.getRetentionIndex();
        double RITolerance = request.getRetentionIndexTolerance(); //%
        double RIToleranceFinal;

        if (RITolerance >= 0 && RITolerance <= 100){
            RIToleranceFinal = RITolerance*0.01;
        } else {
            throw new IllegalArgumentException("Retentention Index Tolerance must be positive(%)" +request.getRetentionIndexTolerance());
        }

        GCMSSearchResponseDTO response = new GCMSSearchResponseDTO();

        double RIDifference = RI * RIToleranceFinal;
        double RILower = RI - RIDifference;
        double RIUpper = RI + RIDifference;

        GCMSFeatureQueryDTO queryData = GCMSFeatureQueryDTO.builder().minRI(RILower).maxRI(RIUpper)
                .derivatizationMethod(derivatizationMethod).columnType(columnType).build();

        try {
            //THE COMPOUNDS ARE ALREADY MERGED
            List<GCMSQueryResponseDTO> queryResults = gcmsSearchRepository.findMatchingCompounds(queryData);

            List<GCMSAnnotation> gcmsAnnotationList = new ArrayList<>();

            for (GCMSQueryResponseDTO queryResult : queryResults) {
                GCMSCompound gcmsCompound = GCMSCompound.builder()
                        .compoundId(queryResult.getCompoundId())
                        .compoundName(queryResult.getCompoundName())
                        .mass(queryResult.getMonoisotopicMass())
                        .formula(queryResult.getFormula())
                        .formulaType(queryResult.getFormulaType())
                        .logP(queryResult.getLogP())
                        .casId(queryResult.getCasId())
                        .chargeType(queryResult.getCharge_type())
                        .chargeNumber(queryResult.getCharge_number())
                        .compoundType(queryResult.getCompound_type())
                        .compoundStatus(queryResult.getCompound_status())
                        .formulaTypeInt(queryResult.getFormula_type_int())
                        .inchi(queryResult.getInchi())
                        .inchiKey(queryResult.getInchiKey())
                        .smiles(queryResult.getSmiles())
                        .dbRI(queryResult.getRI())
                        .derivatizationMethod(queryResult.getDertype())
                        .gcColumn(queryResult.getGcColumn())
                        .GCMSSpectrum(queryResult.getGCMSSpectrum())
                        .build();

                double dbRI = queryResult.getRI();
                //EXPERIMENTAL RI (RI USER) - RI DATABASE -> ABSOLUTE VALUE SO THAT THE RESULT IS POSITIVE
                double deltaRI = Math.abs(RI - dbRI);

                GCMSAnnotation gcmsAnnotation = GCMSAnnotation.builder().gcmsCompound(gcmsCompound)
                        .experimentalRI(RI).deltaRI(deltaRI)
                        .build();

                //CALCULATES THE SCORE WITH THE EXPERIMENTAL SPECTRUM
                gcmsAnnotation.cosineScoreFunction(gcmsSpectrumExperimental);

                gcmsAnnotationList.add(gcmsAnnotation);

            }

            GCMSFeature gcmsFeature = GCMSFeature.builder()
                    .gcmsAnnotations(gcmsAnnotationList)
                    .gcmsSpectrumExperimental(gcmsSpectrumExperimental)
                    .RIExperimental(RI)
                    .build();

            response.addGcmsFeatures(gcmsFeature);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }

}
