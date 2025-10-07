package ceu.biolab.cmm.CEMSMarkers.service;

import ceu.biolab.cmm.CEMSMarkers.domain.MassMode;
import ceu.biolab.cmm.CEMSMarkers.domain.MarkerMobility;
import ceu.biolab.cmm.CEMSMarkers.dto.CemsMarkersRequestDTO;
import ceu.biolab.cmm.CEMSMarkers.repository.CemsMarkersRepository;
import ceu.biolab.cmm.CEMSSearch.dto.CemsSearchResponseDTO;
import ceu.biolab.cmm.CEMSSearch.service.CemsSearchService;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class Cems1MarkerService extends AbstractCemsMarkerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(Cems1MarkerService.class);

    public Cems1MarkerService(CemsMarkersRepository markersRepository,
                              CemsSearchService cemsSearchService) {
        super(markersRepository, cemsSearchService);
    }

    public CemsSearchResponseDTO search(CemsMarkersRequestDTO request) {
        validateRequest(request);

        MarkerMobility markerMobility = markersRepository
                .findMarkerMobility(request.getMarker(), request.getBuffer(), request.getTemperature(), request.getPolarity())
                .orElseThrow(() -> new IllegalArgumentException("Marker mobility not found for marker=" + request.getMarker()
                        + ", buffer=" + request.getBuffer() + ", polarity=" + request.getPolarity().getLabel()
                        + ", temperature=" + request.getTemperature()));

        double markerEffectiveMobility = markerMobility.effectiveMobility();
        double markerTime = request.getMarkerTime();
        double capillaryLength = request.getCapillaryLength();
        double capillaryVoltage = request.getCapillaryVoltage();
        double lengthOverField = (capillaryLength * capillaryLength) / capillaryVoltage;

        List<Double> effectiveMobilities = new ArrayList<>(request.getMigrationTimes().size());
        for (double migrationTime : request.getMigrationTimes()) {
            effectiveMobilities.add(calculateEffectiveMobility(markerEffectiveMobility, lengthOverField, markerTime, migrationTime));
        }

        double mobilityTolerancePercent = computeMobilityTolerancePercent(
                request.getMigrationTimes(),
                request.getMigrationTimeTolerance(),
                request.getMtToleranceMode(),
                time -> calculateEffectiveMobility(markerEffectiveMobility, lengthOverField, markerTime, time)
        );

        return cemsSearchService.search(buildCemsSearchRequest(
                request.getMasses(),
                effectiveMobilities,
                request.getAdducts(),
                request.getChemicalAlphabet(),
                request.getBuffer(),
                request.getMassMode(),
                request.getTolerance(),
                request.getToleranceMode(),
                mobilityTolerancePercent,
                request.getPolarity(),
                request.getIonMode()
        ));
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

    private void validateRequest(CemsMarkersRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Request payload cannot be null");
        }

        validateMassesAndMigration(request.getMasses(), request.getMigrationTimes(), request.getAdducts());

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
