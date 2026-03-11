package ceu.biolab.cmm.adapters.msmsSearchAdapter.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

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
import ceu.biolab.cmm.adapters.shared.domain.LegacyPeak;

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

    private MSMSSearchRequestDTO adaptRequest(MsmsSearchAdapterRequestDTO request) {

        IonizationMode ionizationMode = mapIonMode(request.getIonMode());
        CIDEnergy cidEnergy = mapCidEnergyFromLegacy(request.getIonizationVoltage());
        List<String> adducts = buildDefaultAdducts(ionizationMode);
        List<Peak> peaks = convertLegacyPeaks(request.getMsMsPeaks());
        Spectrum spectrum = buildSpectrum(request.getIonMass(), peaks);


        return new MSMSSearchRequestDTO(
            cidEnergy,
            request.getIonMass(),
            request.getPrecursorIonTolerance(),
            request.getPrecursorIonToleranceMode(),
            request.getPrecursorMzTolerance(),
            request.getPrecursorMzToleranceMode(),
            ionizationMode,
            adducts,
            spectrum,
            ScoreType.COSINE
        );
    }

    /**
     * Converts a list of LegacyPeak to a list of Peak.
     */
    public static List<Peak> convertLegacyPeaks(List<LegacyPeak> legacyPeaks) {
        List<Peak> peaks = new ArrayList<>();
        if (legacyPeaks != null) {
            for (LegacyPeak lp : legacyPeaks) {
                if (lp != null) {
                    peaks.add(new Peak(lp.getMz(), lp.getIntensity()));
                }
            }
        }
        return peaks;
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

      /**
     * Converts LegacyIonizationVoltage to CIDEnergy.
     */
    private CIDEnergy mapCidEnergyFromLegacy(LegacyIonizationVoltage legacy) {
        if (legacy == null) return CIDEnergy.MED;
        switch (legacy) {
            case LOW:
                return CIDEnergy.LOW;
            case HIGH:
                return CIDEnergy.HIGH;
            case MEDIUM:
                return CIDEnergy.MED;
            case ALL:
                return CIDEnergy.ALL;
            default:
                return CIDEnergy.MED;
        }
    }
}

