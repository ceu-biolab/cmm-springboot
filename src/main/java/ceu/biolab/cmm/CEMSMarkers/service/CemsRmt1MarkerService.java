package ceu.biolab.cmm.CEMSMarkers.service;

import ceu.biolab.cmm.CEMSMarkers.domain.MarkerMobility;
import ceu.biolab.cmm.CEMSMarkers.dto.CemsRmtMarkersRequestDTO;
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
public class CemsRmt1MarkerService extends AbstractCemsMarkerService {

    public CemsRmt1MarkerService(CemsMarkersRepository markersRepository,
                                 CemsSearchService cemsSearchService) {
        super(markersRepository, cemsSearchService);
    }

    public CemsSearchResponseDTO search(CemsRmtMarkersRequestDTO request) {
        validateRequest(request);

        MarkerMobility markerMobility = markersRepository
                .findMarkerMobility(request.getMarker(), request.getBuffer(), request.getTemperature(), request.getPolarity())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Marker mobility not found for marker=" + request.getMarker()
                ));

        MarkerMobility referenceMobility = markersRepository
                .findMarkerMobility(request.getRmtReference(), request.getBuffer(), request.getTemperature(), request.getPolarity())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "RMT reference mobility not found for rmt_reference=" + request.getRmtReference()
                ));

        double markerEffectiveMobility = markerMobility.effectiveMobility();
        double markerTime = request.getMarkerTime();
        double lengthOverField = (request.getCapillaryLength() * request.getCapillaryLength())
                / request.getCapillaryVoltage();
        double referenceMigrationTime = calculateMigrationTime(
                referenceMobility.effectiveMobility(), markerEffectiveMobility, lengthOverField, markerTime);

        List<Double> effectiveMobilities = new ArrayList<>(request.getRelativeMigrationTimes().size());
        for (double rmt : request.getRelativeMigrationTimes()) {
            effectiveMobilities.add(calculateEffectiveMobility(
                    markerEffectiveMobility, lengthOverField, markerTime, referenceMigrationTime * rmt));
        }

        double mobilityTolerancePercent = computeMobilityTolerancePercentFromRmt(
                request.getRelativeMigrationTimes(),
                request.getRmtTolerance(),
                request.getRmtToleranceMode(),
                referenceMigrationTime,
                time -> calculateEffectiveMobility(markerEffectiveMobility, lengthOverField, markerTime, time)
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

    private double calculateEffectiveMobility(double markerMobility,
                                              double lengthOverField,
                                              double markerTime,
                                              double migrationTime) {
        if (migrationTime <= 0d) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Migration time must be positive");
        }
        return markerMobility + lengthOverField * ((1d / migrationTime) - (1d / markerTime));
    }

    private double calculateMigrationTime(double effectiveMobility,
                                          double markerMobility,
                                          double lengthOverField,
                                          double markerTime) {
        double denominator = ((effectiveMobility - markerMobility) / lengthOverField) + (1d / markerTime);
        if (denominator == 0d) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid parameters leading to zero denominator in RMT reference calculation");
        }
        double migrationTime = 1d / denominator;
        if (migrationTime <= 0d) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "RMT reference migration time must be positive");
        }
        return migrationTime;
    }

    private void validateRequest(CemsRmtMarkersRequestDTO request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request payload cannot be null");
        }

        validateMassesAndRmt(request.getMasses(), request.getRelativeMigrationTimes(), request.getAdducts());

        if (request.getBuffer() == null || request.getBuffer().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Buffer must be provided");
        }
        if (request.getMarker() == null || request.getMarker().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Marker must be provided");
        }
        if (request.getRmtReference() == null || request.getRmtReference().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "rmt_reference is required");
        }
        if (request.getMarkerTime() <= 0d) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Marker migration time must be positive");
        }
        if (request.getCapillaryLength() <= 0d) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Capillary length must be positive");
        }
        if (request.getCapillaryVoltage() <= 0d) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Capillary voltage must be positive");
        }
        validateCommonSearchFields(request);
    }

    private void validateCommonSearchFields(CemsRmtMarkersRequestDTO request) {
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
