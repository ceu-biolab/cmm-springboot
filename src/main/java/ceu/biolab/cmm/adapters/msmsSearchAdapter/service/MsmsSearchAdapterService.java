package ceu.biolab.cmm.adapters.msmsSearchAdapter.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ceu.biolab.cmm.MSMSSearch.domain.CIDEnergy;
import ceu.biolab.cmm.MSMSSearch.domain.MSMSAnnotation;
import ceu.biolab.cmm.MSMSSearch.domain.Spectrum;
import ceu.biolab.cmm.MSMSSearch.dto.MSMSSearchRequestDTO;
import ceu.biolab.cmm.MSMSSearch.dto.MSMSSearchResponseDTO;
import ceu.biolab.cmm.MSMSSearch.service.MSMSSearchService;
import ceu.biolab.cmm.adapters.msmsSearchAdapter.dto.MsmsSearchAdapterRequestDTO;
import ceu.biolab.cmm.adapters.msmsSearchAdapter.dto.MsmsSearchAdapterResponseDTO;
import ceu.biolab.cmm.adapters.msmsSearchAdapter.dto.MsmsSearchAdapterResponseDTO.MsmsCompound;
import ceu.biolab.cmm.adapters.shared.domain.LegacyIonMode;
import ceu.biolab.cmm.adapters.shared.domain.LegacyIonizationVoltage;
import ceu.biolab.cmm.adapters.shared.domain.SpectraType;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;
import ceu.biolab.cmm.shared.domain.compound.CMMCompound;
import ceu.biolab.cmm.shared.domain.compound.Compound;
import ceu.biolab.cmm.shared.domain.compound.Pathway;
import ceu.biolab.cmm.shared.domain.msFeature.MSPeak;
import ceu.biolab.cmm.shared.domain.msFeature.ScoreType;
import ceu.biolab.cmm.shared.domain.msFeature.Peak;

/**
 * MS/MS Search Adapter Service.
 *
 * Adapts legacy MS/MS search requests to the internal MSMSSearchService
 * and maps its results back to the adapter response format.
 */

@Service
public class MsmsSearchAdapterService {

    @Autowired
    private MSMSSearchService msmsSearchService;

    /**
     * Main entry point for the MS/MS search adapter.
     *
     * @param request MsmsSearchAdapterRequestDTO containing search parameters
     * @return MsmsSearchAdapterResponseDTO with search results
     */
    public MsmsSearchAdapterResponseDTO search(MsmsSearchAdapterRequestDTO request) {
        MSMSSearchRequestDTO internalRequest = adaptRequest(request);
        MSMSSearchResponseDTO internalResponse = msmsSearchService.search(internalRequest);
        return adaptResponse(internalResponse);
    }

    /**
     * First translation: adapter request -> internal MSMSSearchRequestDTO.
     *
     * For now this is a hardcoded request matching a known-good
     * internal payload, ignoring the incoming adapter request.
     */
    private MSMSSearchRequestDTO adaptRequest(MsmsSearchAdapterRequestDTO request) {

        // Hardcoded peaks from the provided JSON
        List<MSPeak> peaks = new ArrayList<>();
        peaks.add(new MSPeak(55.301, 12.753));
        peaks.add(new MSPeak(67.237, 14.611));
        peaks.add(new MSPeak(69.204, 39.189));
        peaks.add(new MSPeak(79.134, 14.527));
        peaks.add(new MSPeak(81.102, 26.351));
        peaks.add(new MSPeak(83.17, 13.007));
        peaks.add(new MSPeak(91.118, 12.331));
        peaks.add(new MSPeak(93.14, 30.405));
        peaks.add(new MSPeak(95.091, 50.0));
        peaks.add(new MSPeak(96.871, 15.034));
        peaks.add(new MSPeak(105.084, 27.365));
        peaks.add(new MSPeak(107.052, 25.0));
        peaks.add(new MSPeak(109.035, 31.757));
        peaks.add(new MSPeak(111.057, 18.074));
        peaks.add(new MSPeak(119.012, 20.777));
        peaks.add(new MSPeak(121.035, 100.0));
        peaks.add(new MSPeak(121.722, 11.318));
        peaks.add(new MSPeak(122.549, 15.456));
        peaks.add(new MSPeak(124.954, 15.203));
        peaks.add(new MSPeak(130.958, 10.98));
        peaks.add(new MSPeak(132.972, 31.419));
        peaks.add(new MSPeak(134.987, 21.199));
        peaks.add(new MSPeak(137.048, 26.689));
        peaks.add(new MSPeak(143.036, 9.544));
        peaks.add(new MSPeak(145.113, 14.949));
        peaks.add(new MSPeak(146.854, 15.034));
        peaks.add(new MSPeak(148.939, 11.74));
        peaks.add(new MSPeak(150.992, 27.027));
        peaks.add(new MSPeak(157.2, 13.851));
        peaks.add(new MSPeak(159.066, 16.639));
        peaks.add(new MSPeak(161.08, 16.639));
        peaks.add(new MSPeak(163.149, 12.078));
        peaks.add(new MSPeak(165.094, 8.108));
        peaks.add(new MSPeak(171.028, 12.331));
        peaks.add(new MSPeak(173.152, 10.557));
        peaks.add(new MSPeak(174.916, 12.584));
        peaks.add(new MSPeak(185.099, 12.5));
        peaks.add(new MSPeak(199.295, 8.024));
        peaks.add(new MSPeak(216.966, 12.078));
        peaks.add(new MSPeak(244.947, 13.936));

        Spectrum spectrum = new Spectrum(287.23, peaks);

        List<String> adducts = new ArrayList<>();
        adducts.add("[M+H]+");

        return new MSMSSearchRequestDTO(
                CIDEnergy.MED,
                287.236,
                10.0,
                MzToleranceMode.PPM,
                30.0,
                MzToleranceMode.PPM,
                IonizationMode.POSITIVE,
                adducts,
                spectrum,
                ScoreType.COSINE);
    }

    /**
     * Second translation: internal MSMSSearchResponseDTO -> adapter response DTO.
     */
    private MsmsSearchAdapterResponseDTO adaptResponse(MSMSSearchResponseDTO internalResponse) {

        List<MsmsCompound> results = new ArrayList<>();

        if (internalResponse != null && internalResponse.getMsmsList() != null) {
            for (MSMSAnnotation annotation : internalResponse.getMsmsList()) {
                if (annotation == null || annotation.getCompound() == null) {
                    continue;
                }

                Compound compound = annotation.getCompound();
                SpectraType spectraType = SpectraType.EXPERIMENTAL; // current adapter only handles experimental spectra
                String hmdbId = "";
                String hmdbUri = "";
                if (compound instanceof CMMCompound) {
                    CMMCompound cmm = (CMMCompound) compound;
                    if (cmm.getHmdbID() != null) {
                        hmdbId = cmm.getHmdbID();
                        hmdbUri = "http://www.hmdb.ca/metabolites/" + cmm.getHmdbID();
                    }
                }

                Double score = annotation.getMsmsCosineScore();

                MsmsCompound msmsCompound = MsmsCompound.builder()
                        .spectralDisplayTools(spectraType)
                        .identifier(compound.getCompoundId())
                        .hmdbCompound(hmdbId)
                        .hmdbUri(hmdbUri)
                        .name(compound.getCompoundName())
                        .formula(compound.getFormula())
                        .mass(compound.getMass())
                        .score(score)
                        .build();

                results.add(msmsCompound);
            }
        }

        return MsmsSearchAdapterResponseDTO.builder()
                .results(results)
                .build();
    }

    private IonizationMode mapIonMode(LegacyIonMode legacy) {
        if (legacy == null) {
            return IonizationMode.POSITIVE;
        }
        switch (legacy) {
            case NEGATIVE:
                return IonizationMode.NEGATIVE;
            case POSITIVE:
            default:
                return IonizationMode.POSITIVE;
        }
    }

    private CIDEnergy mapCidEnergy(LegacyIonizationVoltage legacy) {
        if (legacy == null) {
            return CIDEnergy.MED;
        }
        switch (legacy) {
            case LOW:
                return CIDEnergy.LOW;
            case HIGH:
                return CIDEnergy.HIGH;
            case MEDIUM:
            case ALL: // Map "all" to a medium CID energy
            default:
                return CIDEnergy.MED;
        }
    }

    private List<String> buildDefaultAdducts(IonizationMode ionMode) {
        List<String> adducts = new ArrayList<>();
        if (ionMode == IonizationMode.NEGATIVE) {
            adducts.add("[M-H]-");
        } else {
            adducts.add("[M+H]+");
        }
        return adducts;
    }

    private Spectrum buildSpectrum(double precursorMz, List<Peak> peaks) {
        List<MSPeak> msPeaks = new ArrayList<>();
        if (peaks != null) {
            for (Peak p : peaks) {
                if (p == null) {
                    continue;
                }
                msPeaks.add(new MSPeak(p.getMzValue(), p.getIntensity()));
            }
        }
        return new Spectrum(precursorMz, msPeaks);
    }
}

