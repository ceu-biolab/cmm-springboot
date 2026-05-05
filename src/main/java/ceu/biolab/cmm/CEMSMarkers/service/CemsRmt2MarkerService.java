package ceu.biolab.cmm.CEMSMarkers.service;

import ceu.biolab.cmm.CEMSMarkers.domain.MarkerMobility;
import ceu.biolab.cmm.CEMSMarkers.dto.CemsRmtMarkersTwoRequestDTO;
import ceu.biolab.cmm.CEMSMarkers.repository.CemsMarkersRepository;
import ceu.biolab.cmm.CEMSSearch.dto.CemsSearchResponseDTO;
import ceu.biolab.cmm.CEMSSearch.service.CemsSearchService;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.validation.MzToleranceLimits;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CemsRmt2MarkerService extends AbstractCemsMarkerService {

    public CemsRmt2MarkerService(CemsMarkersRepository markersRepository,
                                 CemsSearchService cemsSearchService) {
        super(markersRepository, cemsSearchService);
    }

    public CemsSearchResponseDTO search(CemsRmtMarkersTwoRequestDTO request) {
        validateRequest(request);

        MarkerMobility marker1Mobility = markersRepository
                .findMarkerMobility(request.getMarker1(), request.getBuffer(), request.getTemperature(), request.getPolarity())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Marker mobility not found for marker1=" + request.getMarker1()
                ));

        MarkerMobility marker2Mobility = markersRepository
                .findMarkerMobility(request.getMarker2(), request.getBuffer(), request.getTemperature(), request.getPolarity())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Marker mobility not found for marker2=" + request.getMarker2()
                ));

        MarkerMobility referenceMobility = markersRepository
                .findMarkerMobility(request.getRmtReference(), request.getBuffer(), request.getTemperature(), request.getPolarity())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "RMT reference mobility not found for rmt_reference=" + request.getRmtReference()
                ));

        if (!marker1Mobility.bufferCode().equals(marker2Mobility.bufferCode())
                || !marker1Mobility.bufferCode().equals(referenceMobility.bufferCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Marker and RMT reference mobilities retrieved with different buffer codes");
        }

        double marker1Time = request.getMarker1Time();
        double marker2Time = request.getMarker2Time();
        if (marker1Time == marker2Time) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Marker migration times must differ for two-marker RMT calibration");
        }

        double mu1 = marker1Mobility.effectiveMobility();
        double mu2 = marker2Mobility.effectiveMobility();
        double referenceMigrationTime = calculateMigrationTime(referenceMobility.effectiveMobility(), mu1, mu2, marker1Time, marker2Time);

        List<Double> effectiveMobilities = new ArrayList<>(request.getRelativeMigrationTimes().size());
        for (double rmt : request.getRelativeMigrationTimes()) {
            effectiveMobilities.add(calculateEffectiveMobility(mu1, mu2, marker1Time, marker2Time, referenceMigrationTime * rmt));
        }

        double mobilityTolerancePercent = computeMobilityTolerancePercentFromRmt(
                request.getRelativeMigrationTimes(),
                request.getRmtTolerance(),
                request.getRmtToleranceMode(),
                referenceMigrationTime,
                time -> calculateEffectiveMobility(mu1, mu2, marker1Time, marker2Time, time)
        );

        return cemsSearchService.search(buildCemsSearchRequest(
                request.getMasses(),
                effectiveMobilities,
                request.getAdducts(),
                request.getChemicalAlphabet(),
                request.getBuffer(),
                request.getTolerance(),
                request.getToleranceMode(),
                request.getTemperature(),
                mobilityTolerancePercent,
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Migration time must be positive");
        }
        double numerator = ((migrationTime - marker2Time) * marker1Time * mu1)
                - ((migrationTime - marker1Time) * marker2Time * mu2);
        double denominator = (marker1Time - marker2Time) * migrationTime;
        if (denominator == 0d) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid parameters leading to zero denominator in mobility calculation");
        }
        return numerator / denominator;
    }

    private double calculateMigrationTime(double effectiveMobility,
                                          double mu1,
                                          double mu2,
                                          double marker1Time,
                                          double marker2Time) {
        double numerator = marker1Time * marker2Time * (mu2 - mu1);
        double denominator = effectiveMobility * (marker1Time - marker2Time)
                - (marker1Time * mu1)
                + (marker2Time * mu2);
        if (denominator == 0d) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid parameters leading to zero denominator in RMT reference calculation");
        }
        double migrationTime = numerator / denominator;
        if (migrationTime <= 0d) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "RMT reference migration time must be positive");
        }
        return migrationTime;
    }

    private void validateRequest(CemsRmtMarkersTwoRequestDTO request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request payload cannot be null");
        }

        validateMassesAndRmt(request.getMasses(), request.getRelativeMigrationTimes(), request.getAdducts());

        if (request.getBuffer() == null || request.getBuffer().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Buffer must be provided");
        }
        if (request.getMarker1() == null || request.getMarker1().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "marker1 must be provided");
        }
        if (request.getMarker2() == null || request.getMarker2().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "marker2 must be provided");
        }
        if (request.getRmtReference() == null || request.getRmtReference().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "rmt_reference is required");
        }
        if (request.getMarker1Time() == null || request.getMarker1Time() <= 0d) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "marker1_time must be positive");
        }
        if (request.getMarker2Time() == null || request.getMarker2Time() <= 0d) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "marker2_time must be positive");
        }
        validateCommonSearchFields(request);
    }

    private void validateCommonSearchFields(CemsRmtMarkersTwoRequestDTO request) {
        if (request.getTemperature() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Temperature must be provided");
        }
        if (request.getTemperature() <= 0d) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Temperature must be positive");
        }
        if (request.getTolerance() <= 0d) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tolerance must be greater than zero");
        }
        if (request.getToleranceMode() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "tolerance_mode is required");
        }
        if (MzToleranceLimits.exceedsLimit(request.getTolerance(), request.getToleranceMode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    MzToleranceLimits.violationMessage("tolerance", request.getToleranceMode()));
        }
        if (request.getRmtTolerance() <= 0d) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "rmt_tolerance must be greater than zero");
        }
        if (request.getRmtToleranceMode() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "rmt_tolerance_mode is required");
        }
        if (request.getPolarity() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "polarity is required");
        }
        if (request.getIonMode() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ionization mode is required");
        }
        if (request.getIonMode() == IonizationMode.NEUTRAL) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Neutral ionization mode is not supported.");
        }
        if (request.getChemicalAlphabet() == null) {
            request.setChemicalAlphabet("ALL");
        }
    }
}
