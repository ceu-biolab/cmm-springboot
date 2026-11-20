package ceu.biolab.cmm.legacyApi.service;

import ceu.biolab.cmm.legacyApi.dto.*;
import ceu.biolab.cmm.legacyApi.mapper.*;
import ceu.biolab.cmm.msSearch.dto.CompoundSimpleSearchRequestDTO;
import ceu.biolab.cmm.msSearch.service.CompoundService;
import ceu.biolab.cmm.browseSearch.dto.BrowseSearchRequest;
import ceu.biolab.cmm.browseSearch.service.BrowseSearchService;
import ceu.biolab.cmm.MSMSSearch.domain.*;
import ceu.biolab.cmm.MSMSSearch.domain.Spectrum;
import ceu.biolab.cmm.MSMSSearch.dto.MSMSSearchRequestDTO;
import ceu.biolab.cmm.MSMSSearch.service.MSMSSearchService;
import ceu.biolab.cmm.shared.domain.*;
import ceu.biolab.cmm.shared.domain.msFeature.*;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class LegacyMsSearchService {
    private final CompoundService compounds;
    private final BrowseSearchService browse;
    private final MSMSSearchService msms;
    private final LegacyOptionsMapper options;
    private final LegacyResponseMapper responses;
    public LegacyMsSearchService(CompoundService compounds, BrowseSearchService browse, MSMSSearchService msms,
                                 LegacyOptionsMapper options, LegacyResponseMapper responses) {
        this.compounds = compounds; this.browse = browse; this.msms = msms; this.options = options; this.responses = responses;
    }
    public CompoundSimpleSearchRequestDTO request(double mass, LegacySearchOptions input, Optional<FormulaType> alphabet) {
        return options.validated(new CompoundSimpleSearchRequestDTO(options.mz(mass, input), options.toleranceMode(input.getToleranceMode()),
                options.tolerance(input.getTolerance(), input.getToleranceMode()), options.ionMode(input.getIonMode()), options.adducts(input),
                Optional.empty(), alphabet, options.databases(input.getDatabases()), options.metaboliteType(input.getMetabolitesType())));
    }
    public List<AnnotatedFeature> annotate(double mass, LegacySearchOptions input, Optional<FormulaType> alphabet) {
        return compounds.findCompoundsByMz(request(mass, input, alphabet)).getMSFeatures();
    }
    public List<AnnotatedFeature> annotateRequest(CompoundSimpleSearchRequestDTO request) {
        return compounds.findCompoundsByMz(request).getMSFeatures();
    }
    public LegacyResults simple(LegacySimpleRequest input) {
        return responses.annotated(annotate(input.getMass(), input, Optional.empty()), options.ionMode(input.getIonMode()), false);
    }
    public LegacyResults batch(LegacyBatchRequest input) {
        List<AnnotatedFeature> features = new ArrayList<>();
        // Validate the complete request before the first search.
        input.getMasses().forEach(m -> request(m, input, Optional.empty()));
        for (double mass : input.getMasses()) features.addAll(annotate(mass, input, Optional.empty()));
        return responses.annotated(features, options.ionMode(input.getIonMode()), false);
    }
    public LegacyResults browse(LegacyBrowseRequest input) {
        if ((input.getName() == null || input.getName().trim().length() < 3)
                && (input.getFormula() == null || input.getFormula().isBlank())) {
            throw LegacyOptionsMapper.bad("Provide a compound name (min 3 characters) or a formula");
        }
        var request = new BrowseSearchRequest(input.getName(), input.getFormula(), options.databases(input.getDatabases()),
                options.metaboliteType(input.getMetabolitesType()), input.isExactName());
        options.validated(request);
        return new LegacyResults(browse.search(request).getCompoundlist().stream().map(responses::compound).toList());
    }
    public LegacyResults msms(LegacyMsmsRequest input) {
        var mode = options.ionMode(input.getIonMode());
        var energy = switch (LegacyOptionsMapper.key(input.getIonizationVoltage())) {
            case "low" -> CIDEnergy.LOW;
            case "med", "medium" -> CIDEnergy.MED;
            case "high" -> CIDEnergy.HIGH;
            case "all" -> CIDEnergy.ALL;
            default -> throw LegacyOptionsMapper.bad("Unsupported ionization_voltage");
        };
        Set<SpectrumSource> sources = new HashSet<>();
        for (String value : input.getSpectraTypes()) {
            var source = SpectrumSource.fromValue(value);
            if (source == SpectrumSource.ALL) throw LegacyOptionsMapper.bad("spectra_types accepts experimental and predicted");
            sources.add(source);
        }
        var spectrum = new Spectrum(input.getIonMass(), input.getPeaks().stream().map(p -> new MSPeak(p.getMz(), p.getIntensity())).toList());
        var request = new MSMSSearchRequestDTO(energy, input.getIonMass(),
                options.tolerance(input.getPrecursorTolerance(), input.getPrecursorToleranceMode()), options.toleranceMode(input.getPrecursorToleranceMode()),
                options.tolerance(input.getFragmentTolerance(), input.getFragmentToleranceMode()), options.toleranceMode(input.getFragmentToleranceMode()),
                mode, List.of(mode == IonizationMode.POSITIVE ? "[M+H]+" : "[M-H]-"), spectrum, ScoreType.COSINE);
        request.setSpectrumSource(sources.size() == 2 ? SpectrumSource.ALL : sources.iterator().next());
        return responses.msms(msms.search(options.validated(request)));
    }
}
