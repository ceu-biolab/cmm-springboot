package ceu.biolab.cmm.legacyApi.service;

import ceu.biolab.cmm.legacyApi.dto.*;
import ceu.biolab.cmm.legacyApi.mapper.*;
import ceu.biolab.cmm.lcmsSearch.dto.BatchAdvancedSearchRequestDTO;
import ceu.biolab.cmm.lcmsSearch.service.BatchAdvancedSearchService;
import ceu.biolab.cmm.scoreAnnotations.service.ScoreAnnotationsService;
import ceu.biolab.cmm.shared.domain.*;
import ceu.biolab.cmm.shared.domain.msFeature.*;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class LegacyAdvancedSearchService {
    private final BatchAdvancedSearchService lcms;
    private final LegacyMsSearchService ms;
    private final LegacyOptionsMapper options;
    private final LegacyResponseMapper responses;
    public LegacyAdvancedSearchService(BatchAdvancedSearchService lcms, LegacyMsSearchService ms, LegacyOptionsMapper options, LegacyResponseMapper responses) {
        this.lcms = lcms; this.ms = ms; this.options = options; this.responses = responses;
    }
    public LegacyResults single(LegacyAdvancedRequest input) {
        return search(input, List.of(input.getMass()), List.of(input.getRetentionTime()),
                input.getCompositeSpectrum() == null ? List.of() : List.of(input.getCompositeSpectrum()), List.of(), List.of(), List.of(),
                input.getChemicalAlphabet(), input.getDeuterium(), input.getModifiersType(), false);
    }
    public LegacyResults batch(LegacyAdvancedBatchRequest input, boolean v3) {
        return search(input, input.getMasses(), input.getRetentionTimes(), input.getCompositeSpectra(), input.getAllMasses(),
                input.getAllRetentionTimes(), input.getAllCompositeSpectra(), input.getChemicalAlphabet(), input.getDeuterium(), input.getModifiersType(), v3);
    }
    private record Feature(double mz, double rt, Map<Double, Double> spectrum) {}
    private record Identity(double mz, double rt) {}
    private List<Feature> features(LegacySearchOptions input, List<Double> masses, List<Double> times, List<List<LegacyPeak>> spectra) {
        if (masses.size() != times.size() || (spectra != null && !spectra.isEmpty() && spectra.size() != masses.size()))
            throw LegacyOptionsMapper.bad("Masses, retention times and supplied spectra must have matching lengths");
        List<Feature> result = new ArrayList<>();
        for (int i = 0; i < masses.size(); i++) {
            if (!Double.isFinite(times.get(i))) throw LegacyOptionsMapper.bad("Retention times must be finite");
            Map<Double, Double> peaks = new LinkedHashMap<>();
            if (spectra != null && !spectra.isEmpty()) for (LegacyPeak p : spectra.get(i)) {
                if (!Double.isFinite(p.getMz()) || !Double.isFinite(p.getIntensity())) throw LegacyOptionsMapper.bad("Peaks must be finite");
                peaks.put(p.getMz(), p.getIntensity());
            }
            result.add(new Feature(options.mz(masses.get(i), input), times.get(i), peaks));
        }
        return result;
    }
    private LegacyResults search(LegacySearchOptions input, List<Double> masses, List<Double> times, List<List<LegacyPeak>> spectra,
                                 List<Double> allMasses, List<Double> allTimes, List<List<LegacyPeak>> allSpectra,
                                 String alphabet, boolean deuterium, String modifier, boolean v3) {
        var formula = options.alphabet(alphabet, deuterium);
        var modifierType = ModifierType.fromName(modifier);
        List<Feature> selected = features(input, masses, times, spectra);
        List<Feature> background = features(input, allMasses, allTimes, allSpectra);
        Set<Identity> selectedKeys = new HashSet<>();
        selected.forEach(f -> selectedKeys.add(new Identity(f.mz(), f.rt())));
        List<Feature> all = new ArrayList<>(selected);
        for (var f : background) if (!v3 || !selectedKeys.contains(new Identity(f.mz(), f.rt()))) all.add(f);
        var mode = options.ionMode(input.getIonMode());
        var adducts = options.adducts(input);
        var databases = options.databases(input.getDatabases());
        var metabolite = options.metaboliteType(input.getMetabolitesType());
        double tolerance = options.tolerance(input.getTolerance(), input.getToleranceMode());
        List<AnnotatedFeature> results;
        if (all.stream().allMatch(f -> !f.spectrum().isEmpty())) {
            // The current LC-MS service owns adduct detection and scoring.
            var request = new BatchAdvancedSearchRequestDTO(all.stream().map(Feature::mz).toList(), options.toleranceMode(input.getToleranceMode()),
                    tolerance, mode, Optional.empty(), adducts, databases, metabolite, all.stream().map(Feature::rt).toList(),
                    all.stream().map(Feature::spectrum).toList(), options.alphabet(alphabet, false), deuterium, modifier);
            results = lcms.annotateAndScoreCmpoundsByMz(options.validated(request));
        } else {
            // Missing spectra carry no evidence for automatic adduct detection. Score the annotations using the current engine.
            results = new ArrayList<>();
            for (int i = 0; i < all.size(); i++) {
                var feature = all.get(i);
                // Features are already converted to m/z; construct the current request directly to avoid a second conversion.
                var request = new ceu.biolab.cmm.msSearch.dto.CompoundSimpleSearchRequestDTO(feature.mz(), options.toleranceMode(input.getToleranceMode()),
                        tolerance, mode, adducts, Optional.empty(), Optional.of(formula), databases, metabolite);
                var annotated = ms.annotateRequest(options.validated(request));
                for (var f : annotated) f.setFeature(new LCMSFeature(feature.rt(), f.getFeature().getMzValue()));
                results.addAll(annotated);
            }
            if (!results.isEmpty()) {
                var parameters = new ExperimentParameters();
                parameters.setIonMode(Optional.of(mode)); parameters.setModifierType(Optional.of(modifierType));
                ScoreAnnotationsService.scoreAnnotations(results, Optional.of(parameters));
            }
        }
        if (v3) results = results.stream().filter(f -> selectedKeys.contains(new Identity(f.getFeature().getMzValue(), ((ILCMSFeature) f.getFeature()).getRtValue()))).toList();
        return responses.annotated(results, mode, true);
    }
}
