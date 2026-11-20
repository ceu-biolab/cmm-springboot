package ceu.biolab.cmm.CEMSMarkers.service;

import ceu.biolab.cmm.CEMSSearch.domain.MobilityBounds;
import ceu.biolab.cmm.CEMSMarkers.domain.MtToleranceMode;
import ceu.biolab.cmm.CEMSMarkers.repository.CemsMarkersRepository;
import ceu.biolab.cmm.CEMSSearch.domain.CePolarity;
import ceu.biolab.cmm.CEMSSearch.domain.EffMobToleranceMode;
import ceu.biolab.cmm.CEMSSearch.domain.RmtToleranceMode;
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
        if (masses.stream().anyMatch(value -> value == null || value <= 0d)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "masses must contain positive numbers only");
        }
        if (migrationTimes.stream().anyMatch(value -> value == null || value <= 0d)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "mt values must contain positive numbers only");
        }
    }

    protected void validateMassesAndRmt(List<Double> masses,
                                        List<Double> relativeMigrationTimes,
                                        List<String> adducts) {
        int massesSize = masses == null ? 0 : masses.size();
        int rmtSize = relativeMigrationTimes == null ? 0 : relativeMigrationTimes.size();
        if (massesSize == 0 || rmtSize == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Both masses and rmt arrays must contain at least one value");
        }
        if (massesSize != rmtSize) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The number of masses must match the number of rmt values");
        }
        if (adducts == null || adducts.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one adduct must be provided");
        }
        if (masses.stream().anyMatch(value -> value == null || value <= 0d)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "masses must contain positive numbers only");
        }
        if (relativeMigrationTimes.stream().anyMatch(value -> value == null || value <= 0d)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "rmt values must contain positive numbers only");
        }
    }

    protected String ensureChemicalAlphabet(String chemicalAlphabet) {
        return chemicalAlphabet == null ? "ALL" : chemicalAlphabet;
    }

    protected List<MobilityBounds> computeMobilityBounds(
            List<Double> migrationTimes, double tolerance, MtToleranceMode mode,
            DoubleUnaryOperator mobilityFunction) {
        List<MobilityBounds> bounds = new ArrayList<>();
        for (double time : migrationTimes) {
            double delta = mode == MtToleranceMode.PERCENTAGE ? time * tolerance / 100d : tolerance;
            bounds.add(transformedBounds(Math.max(time - delta, MIN_TIME_MINUTES), time + delta, mobilityFunction));
        }
        return bounds;
    }

    protected List<MobilityBounds> computeMobilityBoundsFromRmt(
            List<Double> relativeTimes, double tolerance, RmtToleranceMode mode,
            double referenceMigrationTime, DoubleUnaryOperator mobilityFunction) {
        List<MobilityBounds> bounds = new ArrayList<>();
        for (double rmt : relativeTimes) {
            double delta = mode == RmtToleranceMode.PERCENTAGE ? rmt * tolerance / 100d : tolerance;
            bounds.add(transformedBounds(referenceMigrationTime * Math.max(rmt - delta, MIN_TIME_MINUTES),
                    referenceMigrationTime * (rmt + delta), mobilityFunction));
        }
        return bounds;
    }

    private MobilityBounds transformedBounds(
            double lowerTime, double upperTime, DoubleUnaryOperator mobilityFunction) {
        double first = mobilityFunction.applyAsDouble(lowerTime);
        double second = mobilityFunction.applyAsDouble(upperTime);
        return new MobilityBounds(Math.min(first, second), Math.max(first, second));
    }

    protected CemsSearchRequestDTO buildCemsSearchRequest(List<Double> masses,
                                                          List<Double> effectiveMobilities,
                                                          List<String> adducts,
                                                          String chemicalAlphabet,
                                                          String bufferCode,
                                                          double mzTolerance,
                                                          MzToleranceMode toleranceMode,
                                                          Double temperature,
                                                          List<MobilityBounds> mobilityBounds,
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
        // Marker searches supply exact internal bounds; the public tolerance fields
        // remain populated for compatibility with the common request validator.
        requestDTO.setEffectiveMobilityTolerance(1d);
        requestDTO.setMobilityBounds(mobilityBounds);
        requestDTO.setTemperature(temperature);
        requestDTO.setEffectiveMobilityToleranceMode(EffMobToleranceMode.ABSOLUTE);
        return requestDTO;
    }
}
