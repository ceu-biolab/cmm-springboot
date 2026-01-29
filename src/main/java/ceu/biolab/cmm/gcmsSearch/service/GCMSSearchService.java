package ceu.biolab.cmm.gcmsSearch.service;

import ceu.biolab.cmm.gcmsSearch.domain.*;
import ceu.biolab.cmm.gcmsSearch.dto.GCMSFeatureQueryDTO;
import ceu.biolab.cmm.gcmsSearch.dto.GCMSQueryResponseDTO;
import ceu.biolab.cmm.gcmsSearch.dto.GCMSSearchRequestDTO;
import ceu.biolab.cmm.gcmsSearch.dto.GCMSSearchResponseDTO;
import ceu.biolab.cmm.gcmsSearch.repository.GCMSSearchRepository;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;
import ceu.biolab.cmm.shared.domain.msFeature.MSPeak;
import ceu.biolab.cmm.shared.domain.msFeature.Peak;
import ceu.biolab.cmm.shared.domain.msFeature.Spectrum;
import ceu.biolab.cmm.shared.service.SpectrumScorer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class GCMSSearchService {

    private static final double GCMS_SCORE_THRESHOLD = 0.3;
    // TODO: make this configurable, probably in the request
    private static final double GCMS_MZ_TOLERANCE_MDA = 100.0;

    @Autowired
    private GCMSSearchRepository gcmsSearchRepository;

    public GCMSSearchResponseDTO search(GCMSSearchRequestDTO request) {

        Spectrum gcmsSpectrumExperimental = request.getGcmsSpectrumExperimental();
        List<MSPeak> experimentalPeaks = toMsPeaks(gcmsSpectrumExperimental);
        SpectrumScorer spectrumScorer = new SpectrumScorer(MzToleranceMode.MDA, GCMS_MZ_TOLERANCE_MDA);

        ColumnType columnType = request.getColumnType();
        DerivatizationMethod derivatizationMethod = request.getDerivatizationMethod();
        double RI = request.getRetentionIndex();
        double RITolerance = request.getRetentionIndexTolerance(); //%
        if (RI <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Retention index must be greater than zero.");
        }
        if (RITolerance <= 0 || RITolerance > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Retention index tolerance must be between 0 and 100%: " + request.getRetentionIndexTolerance());
        }
        double RIToleranceFinal = RITolerance * 0.01;

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
                        .inchi(queryResult.getInchi())
                        .inchiKey(queryResult.getInchiKey())
                        .smiles(queryResult.getSmiles())
                        .keggID(queryResult.getKeggID())
                        .lmID(queryResult.getLmID())
                        .hmdbID(queryResult.getHmdbID())
                        .agilentID(queryResult.getAgilentID())
                        .pcID(queryResult.getPcID())
                        .chebiID(queryResult.getChebiID())
                        .inHouseID(queryResult.getInHouseID())
                        .aspergillusID(queryResult.getAspergillusID())
                        .knapsackID(queryResult.getKnapsackID())
                        .npatlasID(queryResult.getNpatlasID())
                        .fahfaID(queryResult.getFahfaID())
                        .ohPositionID(queryResult.getOhPositionID())
                        .aspergillusWebName(queryResult.getAspergillusWebName())
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

                double score = gcmsAnnotation.computeCosineScore(experimentalPeaks, spectrumScorer);
                if (score >= GCMS_SCORE_THRESHOLD) {
                    gcmsAnnotationList.add(gcmsAnnotation);
                }

            }

            gcmsAnnotationList.sort(Comparator.comparingDouble(GCMSAnnotation::getGcmsCosineScore).reversed());

            GCMSFeature gcmsFeature = GCMSFeature.builder()
                    .gcmsAnnotations(gcmsAnnotationList)
                    .gcmsSpectrumExperimental(gcmsSpectrumExperimental)
                    .RIExperimental(RI)
                    .build();

            response.addGcmsFeatures(gcmsFeature);

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to execute GC-MS search", e);
        }

        return response;
    }

    private List<MSPeak> toMsPeaks(Spectrum spectrum) {
        List<MSPeak> converted = new ArrayList<>();
        if (spectrum == null || spectrum.getSpectrum() == null) {
            return converted;
        }
        for (Peak peak : spectrum.getSpectrum()) {
            converted.add(new MSPeak(peak.getMzValue(), peak.getIntensity()));
        }
        return converted;
    }
}
