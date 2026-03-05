package ceu.biolab.cmm.adapters.advancedBatchAdapter.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import ceu.biolab.cmm.adapters.advancedBatchAdapter.dto.AdvancedBatchAdapterRequestDTO;
import ceu.biolab.cmm.adapters.advancedBatchAdapter.dto.AdvancedBatchAdapterResponseDTO;
import ceu.biolab.cmm.adapters.advancedBatchAdapter.dto.AdvancedBatchAdapterResponseDTO.PutativeAnnotation;
import ceu.biolab.cmm.adapters.shared.dto.SpectrumDTO;
import ceu.biolab.cmm.adapters.shared.translators.AdductTranslation;
import ceu.biolab.cmm.lcmsSearch.dto.BatchAdvancedSearchRequestDTO;
import ceu.biolab.cmm.lcmsSearch.service.BatchAdvancedSearchService;
import ceu.biolab.cmm.scoreAnnotations.domain.CompoundScores;
import ceu.biolab.cmm.scoreAnnotations.domain.LipidScores;
import ceu.biolab.cmm.adapters.shared.domain.LegacyDatabase;
import ceu.biolab.cmm.adapters.shared.domain.LegacyMassesMode;
import ceu.biolab.cmm.adapters.shared.domain.LegacyMetaboliteType;
import ceu.biolab.cmm.shared.domain.Database;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.MetaboliteType;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;
import ceu.biolab.cmm.shared.domain.compound.CMMCompound;
import ceu.biolab.cmm.shared.domain.compound.Compound;
import ceu.biolab.cmm.shared.domain.compound.Pathway;
import ceu.biolab.cmm.shared.domain.msFeature.AnnotatedFeature;
import ceu.biolab.cmm.shared.domain.msFeature.Annotation;
import ceu.biolab.cmm.shared.domain.msFeature.AnnotationsByAdduct;
import ceu.biolab.cmm.shared.domain.msFeature.ILCMSFeature;
import ceu.biolab.cmm.shared.domain.msFeature.IScore;

/**
 * Advanced Batch Search Adapter Service.
 *
 * Adapts external advanced batch requests to the internal LCMS batch
 * search service (BatchAdvancedSearchService) and maps the annotated
 * features into AdvancedBatchAdapterResponseDTO.
 */

@Service
public class AdvancedBatchAdapterService {

    // === DEFAULTS FOR INTERNAL LCMS REQUEST (fallbacks only) ===

    private static final MzToleranceMode DEFAULT_TOLERANCE_MODE = MzToleranceMode.PPM;
    private static final double DEFAULT_TOLERANCE = 10.0;
    private static final IonizationMode DEFAULT_ION_MODE = IonizationMode.POSITIVE;
    private static final Set<Database> DEFAULT_DATABASES = Set.of(Database.ALL);
    private static final MetaboliteType DEFAULT_METABOLITE_TYPE = MetaboliteType.ALL;

    private final BatchAdvancedSearchService batchAdvancedSearchService;

    public AdvancedBatchAdapterService(BatchAdvancedSearchService batchAdvancedSearchService) {
        this.batchAdvancedSearchService = batchAdvancedSearchService;
    }

    /**
     * Main entry point for the advanced batch adapter.
     *
     * 1) Validates and transforms the adapter request into the internal
     *    BatchAdvancedSearchRequestDTO.
     * 2) Delegates to BatchAdvancedSearchService.annotateAndScoreCmpoundsByMz.
     * 3) Transforms the resulting annotated features into the adapter
     *    response DTO.
     */
    public AdvancedBatchAdapterResponseDTO search(AdvancedBatchAdapterRequestDTO request) {
        validateRequest(request);
        // Translate external adapter request into internal LCMS request
        BatchAdvancedSearchRequestDTO internalRequest = transformRequest(request);
        List<AnnotatedFeature> features = batchAdvancedSearchService.annotateAndScoreCmpoundsByMz(internalRequest);
        return transformResult(features);
    }

    /**
     * Basic validation for advanced batch requests, mirroring the
     * constraints used in SimpleSearch where applicable.
     */
    private void validateRequest(AdvancedBatchAdapterRequestDTO request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
        }

        Double tol = request.getTolerance();
        if (tol != null && (tol < 0 || tol > 100)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tolerance must be in the range (0,100).");
        }

        if (request.getIonMode() == IonizationMode.NEUTRAL) {
            throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Neutral mode is not implemented yet");
        }

        if (request.getDatabases() != null) {
            for (LegacyDatabase db : request.getDatabases()) {
                if (db == LegacyDatabase.MINE || db == LegacyDatabase.METLIN) {
                    throw new ResponseStatusException(HttpStatus.GONE,
                            "Database " + db.getValue() + " is no longer supported");
                }
            }
        }
    }

    /**
     * Transforms the external adapter request into the internal
     * BatchAdvancedSearchRequestDTO used by the LCMS batch advanced service.
     */
    public BatchAdvancedSearchRequestDTO transformRequest(AdvancedBatchAdapterRequestDTO request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
        }

        // Choose source lists: prefer all_* when present, otherwise primary masses/retention/composite
        List<Double> sourceMasses = Optional.ofNullable(request.getAllMasses()).filter(l -> !l.isEmpty())
                .orElseGet(() -> Optional.ofNullable(request.getMasses()).orElseGet(ArrayList::new));
        List<Double> sourceRts = Optional.ofNullable(request.getAllRetentionTimes()).filter(l -> !l.isEmpty())
                .orElseGet(() -> Optional.ofNullable(request.getRetentionTimes()).orElseGet(ArrayList::new));
        List<List<SpectrumDTO>> sourceComposite = Optional.ofNullable(request.getAllCompositeSpectra()).filter(l -> !l.isEmpty())
                .orElseGet(() -> Optional.ofNullable(request.getCompositeSpectra()).orElseGet(ArrayList::new));

        int size = sourceMasses.size();
        if (size == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one mass is required for advanced batch search.");
        }
        if (sourceRts.size() != size || sourceComposite.size() != size) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Masses, retention_times and composite_spectra lists must have the same size.");
        }

        List<Map<Double, Double>> compositeSpectrum = new ArrayList<>();
        for (List<SpectrumDTO> spectraList : sourceComposite) {
            Map<Double, Double> spectrumMap = new LinkedHashMap<>();
            if (spectraList != null) {
                for (SpectrumDTO spectrum : spectraList) {
                    if (spectrum != null) {
                        spectrumMap.put(spectrum.getMz(), spectrum.getIntensity());
                    }
                }
            }
            compositeSpectrum.add(spectrumMap);
        }

        BatchAdvancedSearchRequestDTO internal = new BatchAdvancedSearchRequestDTO();

        internal.setMz(sourceMasses);
        internal.setRetentionTimes(sourceRts);
        internal.setCompositeSpectrum(compositeSpectrum);

        // Tolerance and ionization from request or defaults
        internal.setMzToleranceMode(Optional.ofNullable(request.getToleranceMode()).orElse(DEFAULT_TOLERANCE_MODE));
        internal.setTolerance(Optional.ofNullable(request.getTolerance()).orElse(DEFAULT_TOLERANCE));
        internal.setIonizationMode(Optional.ofNullable(request.getIonMode()).orElse(DEFAULT_ION_MODE));
        internal.setDetectedAdduct(Optional.empty());

        // Databases and metabolite type via shared translations
        Set<Database> dbs = ceu.biolab.cmm.adapters.shared.translators.DatabaseTranslation
            .toDatabases(Optional.ofNullable(request.getDatabases()).orElseGet(java.util.HashSet::new));
        internal.setDatabases(new LinkedHashSet<>(dbs.isEmpty() ? DEFAULT_DATABASES : dbs));

        MetaboliteType metaboliteType = ceu.biolab.cmm.adapters.shared.translators.MetaboliteTypeTranslation
            .toMetaboliteType(Optional.ofNullable(request.getMetaboliteTypes()).orElse(ceu.biolab.cmm.adapters.shared.domain.LegacyMetaboliteType.ALL_EXCEPT_PEPTIDES));
        internal.setMetaboliteType(Optional.ofNullable(metaboliteType).orElse(DEFAULT_METABOLITE_TYPE));

        // Adducts: translate user-provided or default. The special value
        // "all" means "use a default set of canonical adducts".
        Set<String> adductsRaw = Optional.ofNullable(request.getAdducts()).orElseGet(java.util.HashSet::new);
        boolean containsAll = adductsRaw.stream().anyMatch(a -> a != null && a.equalsIgnoreCase("all"));

        if (adductsRaw.isEmpty() || containsAll) {
            List<String> defaultAdducts = getDefaultCanonicalAdducts(internal.getIonizationMode());
            internal.setAdductsString(new LinkedHashSet<>(defaultAdducts));
        } else {
            List<String> transformedAdducts = ceu.biolab.cmm.adapters.shared.translators.AdductTranslation
                .translateAll(adductsRaw, internal.getIonizationMode());
            if (transformedAdducts.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No valid adducts provided for advanced batch search.");
            }
            internal.setAdductsString(new LinkedHashSet<>(transformedAdducts));
        }
        internal.setFormulaType(Optional.ofNullable(request.getChemicalAlphabet()).orElse(ceu.biolab.cmm.shared.domain.FormulaType.CHNOPS));
        internal.setDeuterium(request.isDeuterium());
        internal.setModifiersType(request.getModifiersType() != null ? request.getModifiersType() : "none");

        return internal;
    }

    /**
     * Default set of canonical adducts to use when the adapter-level
     * request specifies "all" or omits adducts entirely. These values
     * are already in canonical [M+..]+ / [M-..]- form.
     */
    private List<String> getDefaultCanonicalAdducts(IonizationMode mode) {
        switch (mode) {
            case NEGATIVE:
                // Default negative_enum (canonical): ["[M-H]-", "[M+Cl]-", "[M+FA-H]-", "[M-H-H2O]-"]
                return List.of("[M-H]-", "[M+Cl]-", "[M+FA-H]-", "[M-H-H2O]-");
            case NEUTRAL:
                // Neutral is not allowed at validation time, but we
                // still provide a canonical form here for completeness.
                return List.of("[M]");
            case POSITIVE:
            default:
                // Default positive_enum (canonical):
                // ["[M+H]+", "[M+2H]2+", "[M+Na]+", "[M+K]+", "[M+NH4]+", "[M+H-H2O]+"]
                return List.of("[M+H]+", "[M+2H]2+", "[M+Na]+", "[M+K]+", "[M+NH4]+", "[M+H-H2O]+");
        }
    }

    /**
     * Transforms the annotated LCMS features into the AdvancedBatch
     * adapter response DTO (array of putative_annotation_object).
     */
    public AdvancedBatchAdapterResponseDTO transformResult(List<AnnotatedFeature> features) {
        AdvancedBatchAdapterResponseDTO response = new AdvancedBatchAdapterResponseDTO();
        List<PutativeAnnotation> results = new ArrayList<>();
        response.setResults(results);

        if (features == null || features.isEmpty()) {
            return response;
        }

        for (AnnotatedFeature annotatedFeature : features) {
            if (annotatedFeature == null || annotatedFeature.getFeature() == null) {
                continue;
            }

            double em = annotatedFeature.getFeature().getMzValue();
            double rt = 0.0;
            if (annotatedFeature.getFeature() instanceof ILCMSFeature lcFeature) {
                rt = lcFeature.getRtValue();
            }

            List<AnnotationsByAdduct> annotationsByAdducts = annotatedFeature.getAnnotationsByAdducts();
            if (annotationsByAdducts == null || annotationsByAdducts.isEmpty()) {
                continue;
            }

            for (AnnotationsByAdduct group : annotationsByAdducts) {
                if (group == null || group.getAnnotations() == null || group.getAnnotations().isEmpty()) {
                    continue;
                }

                String rawAdduct = group.getAdduct();
                String adductLabel = rawAdduct != null ? AdductTranslation.reverse(rawAdduct) : "";

                for (Annotation annotation : group.getAnnotations()) {
                    if (annotation == null) {
                        continue;
                    }

                    PutativeAnnotation dto = new PutativeAnnotation();

                    dto.setRt(rt);
                    dto.setEm(em);
                    dto.setAdduct(adductLabel);

                    // Default scores to -2 ("no information")
                    int ionizationScoreCat = -2;
                    int adductRelationScoreCat = -2;
                    int rtScoreCat = -2;

                    List<IScore> scores = annotation.getScores();
                    if (scores != null) {
                        for (IScore score : scores) {
                            if (score == null) {
                                continue;
                            }
                            Map<String, String> scoreMap = score.getScores();
                            if (scoreMap == null || scoreMap.isEmpty()) {
                                continue;
                            }

                            String ionRaw = scoreMap.get("ionization");
                            String adductRaw = scoreMap.get("adduct");
                            String rtRaw = scoreMap.get("rt");

                            if (ionRaw != null) {
                                ionizationScoreCat = mapScoreToCategory(ionRaw);
                            }
                            if (adductRaw != null) {
                                adductRelationScoreCat = mapScoreToCategory(adductRaw);
                            }
                            if (rtRaw != null) {
                                rtScoreCat = mapScoreToCategory(rtRaw);
                            }
                        }
                    }

                    dto.setIonizationScore(ionizationScoreCat);
                    dto.setAdductRelationScore(adductRelationScoreCat);
                    dto.setRtScore(rtScoreCat);
                    dto.setFinalScore(computeFinalScore(ionizationScoreCat, adductRelationScoreCat, rtScoreCat));

                    if (annotation.getMassErrorPpm() != null) {
                        dto.setErrorPpm(annotation.getMassErrorPpm().intValue());
                    }

                    Compound compound = annotation.getCompound();
                    if (compound != null) {
                        dto.setIdentifier(compound.getCompoundId());
                        dto.setCas(compound.getCasId() != null ? compound.getCasId() : "");
                        dto.setName(compound.getCompoundName());
                        dto.setFormula(compound.getFormula());
                        dto.setMolecularWeight(compound.getMass());
                        dto.setInChiKey(compound.getInchiKey() != null ? compound.getInchiKey() : "");
                        dto.setPathways(extractPathways(compound.getPathways()));

                        if (compound instanceof CMMCompound cmm) {
                            String keggId = cmm.getKeggID();
                            String hmdbId = cmm.getHmdbID();
                            String lmId = cmm.getLmID();
                            Integer pcId = cmm.getPcID();

                            dto.setKeggCompound(keggId != null ? keggId : "");
                            dto.setKeggUri(keggId != null ? "https://www.kegg.jp/entry/" + keggId : "");

                            dto.setHmdbCompound(hmdbId != null ? hmdbId : "");
                            dto.setHmdbUri(hmdbId != null ? "https://hmdb.ca/metabolites/" + hmdbId : "");

                            dto.setLipidmapsCompound(lmId != null ? lmId : "");
                            dto.setLipidmapsUri(lmId != null ? "https://www.lipidmaps.org/databases/lmissd/" + lmId : "");

                            dto.setPubchemCompound(pcId != null ? pcId.toString() : "");
                            dto.setPubchemUri(pcId != null ? "https://pubchem.ncbi.nlm.nih.gov/compound/" + pcId : "");
                        } else {
                            dto.setKeggCompound("");
                            dto.setKeggUri("");
                            dto.setHmdbCompound("");
                            dto.setHmdbUri("");
                            dto.setLipidmapsCompound("");
                            dto.setLipidmapsUri("");
                            dto.setPubchemCompound("");
                            dto.setPubchemUri("");
                        }

                        // Not supported
                        dto.setMetlinCompound("");
                        dto.setMetlinUri("");
                    }

                    results.add(dto);
                }
            }
        }

        return response;
    }

    private List<String> extractPathways(Set<Pathway> pathways) {
        List<String> list = new ArrayList<>();
        if (pathways == null) {
            return list;
        }
        for (Pathway p : pathways) {
            if (p != null) {
                list.add(p.toString());
            }
        }
        return list;
    }

    private int mapScoreToCategory(String raw) {
        if (raw == null || raw.isBlank()) {
            return -2;
        }
        try {
            double value = Double.parseDouble(raw);
            int rounded = (int) Math.round(value);
            if (rounded < -2) {
                return -2;
            }
            if (rounded > 2) {
                return 2;
            }
            return rounded;
        } catch (NumberFormatException e) {
            return -2;
        }
    }

    private int computeFinalScore(int ionizationScore, int adductRelationScore, int rtScore) {
        int max = Math.max(ionizationScore, Math.max(adductRelationScore, rtScore));
        if (max < -1) {
            return -2;
        }
        return max;
    }
}

