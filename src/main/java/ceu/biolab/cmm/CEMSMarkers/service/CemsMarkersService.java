package ceu.biolab.cmm.CEMSMarkers.service;

import ceu.biolab.cmm.CEMSMarkers.domain.MassMode;
import ceu.biolab.cmm.CEMSMarkers.domain.MarkerMobility;
import ceu.biolab.cmm.CEMSMarkers.dto.CemsMarkersRequestDTO;
import ceu.biolab.cmm.CEMSMarkers.repository.CemsMarkersRepository;
import ceu.biolab.cmm.CEMSSearch.dto.CemsSearchRequestDTO;
import ceu.biolab.cmm.CEMSSearch.dto.CemsSearchResponseDTO;
import ceu.biolab.cmm.CEMSSearch.service.CemsSearchService;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CemsMarkersService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CemsMarkersService.class);
    private static final double MIN_TIME_MINUTES = 1e-6;

    private final CemsMarkersRepository markersRepository;
    private final CemsSearchService cemsSearchService;

    public CemsMarkersService(CemsMarkersRepository markersRepository,
                              CemsSearchService cemsSearchService) {
        this.markersRepository = markersRepository;
        this.cemsSearchService = cemsSearchService;
    }

    public CemsSearchResponseDTO search(CemsMarkersRequestDTO request) {
        validateRequest(request);

        MarkerMobility markerMobility = markersRepository
                .findMarkerMobility(request.getMarker(), request.getBuffer(), request.getTemperature(), request.getPolarity())
                .orElseThrow(() -> new IllegalArgumentException("Marker mobility not found for marker=" + request.getMarker()
                        + ", buffer=" + request.getBuffer() + ", polarity=" + request.getPolarity().getLabel()
                        + ", temperature=" + request.getTemperature()));

        double markerEffectiveMobility = markerMobility.effectiveMobility();
        double capillaryLength = request.getCapillaryLength();
        double capillaryVoltage = request.getCapillaryVoltage();
        double markerTime = request.getMarkerTime();

        double lengthOverField = (capillaryLength * capillaryLength) / capillaryVoltage;

        List<Double> effectiveMobilities = new ArrayList<>(request.getMigrationTimes().size());
        for (double migrationTime : request.getMigrationTimes()) {
            effectiveMobilities.add(calculateEffectiveMobility(markerEffectiveMobility, lengthOverField, markerTime, migrationTime));
        }

        double mobilityTolerancePercent = computeMobilityTolerancePercent(request, markerEffectiveMobility, lengthOverField, effectiveMobilities);

        CemsSearchRequestDTO cemsRequest = new CemsSearchRequestDTO();
        cemsRequest.setBackgroundElectrolyte(request.getBuffer());
        cemsRequest.setPolarity(request.getPolarity().getLabel());
        cemsRequest.setChemicalAlphabet(request.getChemicalAlphabet());
        cemsRequest.setInputMassMode(request.getMassMode().getCemsEquivalent());
        cemsRequest.setIonizationMode(request.getIonMode().name());
        cemsRequest.setAdducts(new ArrayList<>(request.getAdducts()));
        cemsRequest.setMzValues(new ArrayList<>(request.getMasses()));
        cemsRequest.setEffectiveMobilities(effectiveMobilities);
        cemsRequest.setMzTolerance(request.getTolerance());
        cemsRequest.setMzToleranceMode(request.getToleranceMode().name());
        cemsRequest.setEffectiveMobilityTolerance(mobilityTolerancePercent);
        cemsRequest.setBufferIdOverride(markerMobility.bufferId());

        return cemsSearchService.search(cemsRequest);
    }

    private double calculateEffectiveMobility(double markerMobility,
                                              double lengthOverField,
                                              double markerTime,
                                              double migrationTime) {
        if (migrationTime <= 0) {
            throw new IllegalArgumentException("Migration time must be positive");
        }
        double mobility = markerMobility + lengthOverField * ((1d / migrationTime) - (1d / markerTime));
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Computed effective mobility={} for migrationTime={} min", mobility, migrationTime);
        }
        return mobility;
    }

    private double computeMobilityTolerancePercent(CemsMarkersRequestDTO request,
                                                   double markerMobility,
                                                   double lengthOverField,
                                                   List<Double> baselineMobilities) {
        double mtTolerance = request.getMigrationTimeTolerance();
        if (mtTolerance <= 0) {
            return 0d;
        }

        double maxFraction = 0d;
        for (int i = 0; i < request.getMigrationTimes().size(); i++) {
            double baseTime = request.getMigrationTimes().get(i);
            double delta = switch (request.getMtToleranceMode()) {
                case PERCENTAGE -> baseTime * (mtTolerance * 0.01d);
                case ABSOLUTE -> mtTolerance;
            };
            if (delta <= 0) {
                continue;
            }

            double muBase = baselineMobilities.get(i);
            if (muBase == 0d) {
                continue;
            }

            double upperTime = baseTime + delta;
            double lowerTime = Math.max(baseTime - delta, MIN_TIME_MINUTES);

            double muUpper = calculateEffectiveMobility(markerMobility, lengthOverField, request.getMarkerTime(), upperTime);
            double muLower = calculateEffectiveMobility(markerMobility, lengthOverField, request.getMarkerTime(), lowerTime);

            double fracUpper = Math.abs(muUpper - muBase) / Math.abs(muBase);
            double fracLower = Math.abs(muLower - muBase) / Math.abs(muBase);

            maxFraction = Math.max(maxFraction, Math.max(fracUpper, fracLower));
        }

        return maxFraction * 100d;
    }

    private void validateRequest(CemsMarkersRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Request payload cannot be null");
        }

        int massesSize = request.getMasses() == null ? 0 : request.getMasses().size();
        int mtSize = request.getMigrationTimes() == null ? 0 : request.getMigrationTimes().size();
        if (massesSize == 0 || mtSize == 0) {
            throw new IllegalArgumentException("Both masses and mt arrays must contain at least one value");
        }
        if (massesSize != mtSize) {
            throw new IllegalArgumentException("The number of masses must match the number of migration times");
        }
        if (request.getAdducts() == null || request.getAdducts().isEmpty()) {
            throw new IllegalArgumentException("At least one adduct must be provided");
        }
        if (request.getBuffer() == null || request.getBuffer().isBlank()) {
            throw new IllegalArgumentException("Buffer must be provided");
        }
        if (request.getMarker() == null || request.getMarker().isBlank()) {
            throw new IllegalArgumentException("Marker must be provided");
        }
        if (request.getMarkerTime() <= 0d) {
            throw new IllegalArgumentException("Marker migration time must be positive");
        }
        if (request.getCapillaryLength() <= 0d) {
            throw new IllegalArgumentException("Capillary length must be positive");
        }
        if (request.getCapillaryVoltage() <= 0d) {
            throw new IllegalArgumentException("Capillary voltage must be positive");
        }
        if (request.getTemperature() == null) {
            throw new IllegalArgumentException("Temperature must be provided");
        }
        if (request.getTolerance() < 0d) {
            throw new IllegalArgumentException("Tolerance must be non-negative");
        }
        if (request.getChemicalAlphabet() == null) {
            request.setChemicalAlphabet("ALL");
        }
        if (request.getMassMode() == null) {
            request.setMassMode(MassMode.MZ);
        }
    }
}
