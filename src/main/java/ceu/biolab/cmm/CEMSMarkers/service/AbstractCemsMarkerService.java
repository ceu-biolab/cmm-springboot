package ceu.biolab.cmm.CEMSMarkers.service;

import ceu.biolab.cmm.CEMSMarkers.domain.MtToleranceMode;
import ceu.biolab.cmm.CEMSMarkers.repository.CemsMarkersRepository;
import ceu.biolab.cmm.CEMSSearch.domain.CePolarity;
import ceu.biolab.cmm.CEMSSearch.domain.EffMobToleranceMode;
import ceu.biolab.cmm.CEMSSearch.dto.CemsSearchRequestDTO;
import ceu.biolab.cmm.CEMSSearch.service.CemsSearchService;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.DoubleUnaryOperator;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

abstract class AbstractCemsMarkerService {

    protected static final double MIN_TIME_MINUTES = 1e-6;

    protected final CemsMarkersRepository markersRepository;
    protected final CemsSearchService cemsSearchService;

    protected AbstractCemsMarkerService(CemsMarkersRepository markersRepository,
                                        CemsSearchService cemsSearchService) {
        this.markersRepository = markersRepository;
        this.cemsSearchService = cemsSearchService;
    }

    protected void validateMassesAndMigration(List<Double> masses,
                                              List<Double> migrationTimes,
                                              List<String> adducts) {
        int massesSize = masses == null ? 0 : masses.size();
        int mtSize = migrationTimes == null ? 0 : migrationTimes.size();
        if (massesSize == 0 || mtSize == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Both masses and mt arrays must contain at least one value");
        }
        if (massesSize != mtSize) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The number of masses must match the number of migration times");
        }
        if (adducts == null || adducts.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one adduct must be provided");
        }
    }

    protected String ensureChemicalAlphabet(String chemicalAlphabet) {
        return chemicalAlphabet == null ? "ALL" : chemicalAlphabet;
    }

    protected double computeMobilityTolerancePercent(List<Double> migrationTimes,
                                                     double mtTolerance,
                                                     MtToleranceMode toleranceMode,
                                                     DoubleUnaryOperator mobilityFunction) {
        if (mtTolerance <= 0) {
            return 0d;
        }
        double maxFraction = 0d;
        for (double baseTime : migrationTimes) {
            double delta = switch (toleranceMode) {
                case PERCENTAGE -> baseTime * (mtTolerance * 0.01d);
                case ABSOLUTE -> mtTolerance;
            };
            if (delta <= 0) {
                continue;
            }
            double baseMobility = mobilityFunction.applyAsDouble(baseTime);
            if (baseMobility == 0d) {
                continue;
            }
            double upperTime = baseTime + delta;
            double lowerTime = Math.max(baseTime - delta, MIN_TIME_MINUTES);
            double upperMobility = mobilityFunction.applyAsDouble(upperTime);
            double lowerMobility = mobilityFunction.applyAsDouble(lowerTime);
            double fracUpper = Math.abs(upperMobility - baseMobility) / Math.abs(baseMobility);
            double fracLower = Math.abs(lowerMobility - baseMobility) / Math.abs(baseMobility);
            maxFraction = Math.max(maxFraction, Math.max(fracUpper, fracLower));
        }
        return maxFraction * 100d;
    }

    protected CemsSearchRequestDTO buildCemsSearchRequest(List<Double> masses,
                                                          List<Double> effectiveMobilities,
                                                          List<String> adducts,
                                                          String chemicalAlphabet,
                                                          String bufferCode,
                                                          double mzTolerance,
                                                          MzToleranceMode toleranceMode,
                                                          Double temperature,
                                                          double mobilityTolerancePercent,
                                                          CePolarity polarity,
                                                          IonizationMode ionMode) {
        CemsSearchRequestDTO requestDTO = new CemsSearchRequestDTO();
        requestDTO.setBufferCode(bufferCode);
        requestDTO.setChemicalAlphabet(ensureChemicalAlphabet(chemicalAlphabet));
        requestDTO.setIonizationMode(Objects.requireNonNull(ionMode).name());
        requestDTO.setPolarity(polarity.getLabel());
        requestDTO.setAdducts(new ArrayList<>(adducts));
        requestDTO.setMzValues(new ArrayList<>(masses));
        requestDTO.setEffectiveMobilities(effectiveMobilities);
        requestDTO.setMzTolerance(mzTolerance);
        requestDTO.setMzToleranceMode(toleranceMode.name());
        requestDTO.setEffectiveMobilityTolerance(mobilityTolerancePercent);
        requestDTO.setTemperature(temperature);
        requestDTO.setEffectiveMobilityToleranceMode(EffMobToleranceMode.PERCENTAGE);
        return requestDTO;
    }
}
