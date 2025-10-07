package ceu.biolab.cmm.CEMSMarkers.service;

import ceu.biolab.cmm.CEMSMarkers.domain.MassMode;
import ceu.biolab.cmm.CEMSMarkers.domain.MarkerMobility;
import ceu.biolab.cmm.CEMSMarkers.dto.CemsMarkersTwoRequestDTO;
import ceu.biolab.cmm.CEMSMarkers.repository.CemsMarkersRepository;
import ceu.biolab.cmm.CEMSSearch.dto.CemsSearchResponseDTO;
import ceu.biolab.cmm.CEMSSearch.service.CemsSearchService;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class Cems2MarkerService extends AbstractCemsMarkerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(Cems2MarkerService.class);

    public Cems2MarkerService(CemsMarkersRepository markersRepository,
                              CemsSearchService cemsSearchService) {
        super(markersRepository, cemsSearchService);
    }

    public CemsSearchResponseDTO search(CemsMarkersTwoRequestDTO request) {
        validateRequest(request);

        MarkerMobility marker1Mobility = markersRepository
                .findMarkerMobility(request.getMarker1(), request.getBuffer(), request.getTemperature(), request.getPolarity())
                .orElseThrow(() -> new IllegalArgumentException("Marker mobility not found for marker=" + request.getMarker1()));

        MarkerMobility marker2Mobility = markersRepository
                .findMarkerMobility(request.getMarker2(), request.getBuffer(), request.getTemperature(), request.getPolarity())
                .orElseThrow(() -> new IllegalArgumentException("Marker mobility not found for marker=" + request.getMarker2()));

        if (marker1Mobility.bufferId() != marker2Mobility.bufferId()) {
            throw new IllegalArgumentException("Marker mobilities retrieved with different buffer ids");
        }

        double marker1Time = request.getMarker1Time();
        double marker2Time = request.getMarker2Time();
        if (marker1Time == marker2Time) {
            throw new IllegalArgumentException("Marker migration times must differ for two-marker calibration");
        }

        double mu1 = marker1Mobility.effectiveMobility();
        double mu2 = marker2Mobility.effectiveMobility();

        List<Double> effectiveMobilities = new ArrayList<>(request.getMigrationTimes().size());
        for (double migrationTime : request.getMigrationTimes()) {
            effectiveMobilities.add(calculateEffectiveMobility(mu1, mu2, marker1Time, marker2Time, migrationTime));
        }

        double mobilityTolerancePercent = computeMobilityTolerancePercent(
                request.getMigrationTimes(),
                request.getMigrationTimeTolerance(),
                request.getMtToleranceMode(),
                time -> calculateEffectiveMobility(mu1, mu2, marker1Time, marker2Time, time)
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
                marker1Mobility.bufferId(),
                request.getPolarity(),
                request.getIonMode()
        ));
    }

    private double calculateEffectiveMobility(double mu1,
                                              double mu2,
                                              double marker1Time,
                                              double marker2Time,
                                              double migrationTime) {
        if (migrationTime <= 0d) {
            throw new IllegalArgumentException("Migration time must be positive");
        }
        double numerator = ((migrationTime - marker2Time) * marker1Time * mu1)
                - ((migrationTime - marker1Time) * marker2Time * mu2);
        double denominator = (marker1Time - marker2Time) * migrationTime;
        if (denominator == 0d) {
            throw new IllegalArgumentException("Invalid parameters leading to zero denominator in mobility calculation");
        }
        double mobility = numerator / denominator;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Computed effective mobility={} for migrationTime={} min", mobility, migrationTime);
        }
        return mobility;
    }

    private void validateRequest(CemsMarkersTwoRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Request payload cannot be null");
        }

        validateMassesAndMigration(request.getMasses(), request.getMigrationTimes(), request.getAdducts());

        if (request.getBuffer() == null || request.getBuffer().isBlank()) {
            throw new IllegalArgumentException("Buffer must be provided");
        }
        if (request.getMarker1() == null || request.getMarker1().isBlank()) {
            throw new IllegalArgumentException("marker1 must be provided");
        }
        if (request.getMarker2() == null || request.getMarker2().isBlank()) {
            throw new IllegalArgumentException("marker2 must be provided");
        }
        if (request.getMarker1Time() <= 0d) {
            throw new IllegalArgumentException("marker1_time must be positive");
        }
        if (request.getMarker2Time() <= 0d) {
            throw new IllegalArgumentException("marker2_time must be positive");
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
