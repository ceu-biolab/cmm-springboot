package ceu.biolab.cmm.legacyApi.mapper;

import ceu.biolab.cmm.legacyApi.dto.LegacyCeRequest;
import ceu.biolab.cmm.CEMSSearch.dto.*;
import ceu.biolab.cmm.CEMSMarkers.dto.*;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class LegacyCeRequestMapper {
    private final LegacyOptionsMapper options;
    public LegacyCeRequestMapper(LegacyOptionsMapper options) { this.options = options; }
    public String buffer(LegacyCeRequest input) {
        return switch (LegacyOptionsMapper.key(input.getBuffer())) {
            case "formic-acid-1m-20c" -> "FORMIC_ACID_1M";
            case "acetic-acid-1m-25c" -> "ACETIC_ACID_10PERCENT";
            default -> throw LegacyOptionsMapper.bad("Unsupported legacy buffer: " + input.getBuffer());
        };
    }
    private double temperature(LegacyCeRequest input) { return buffer(input).equals("FORMIC_ACID_1M") ? 20d : 25d; }
    private List<Double> masses(LegacyCeRequest input) { return input.getMasses().stream().map(m -> options.mz(m, input)).toList(); }
    private String reference(String value) {
        if (value == null || value.isBlank()) throw LegacyOptionsMapper.bad("Marker/reference is required");
        return switch (LegacyOptionsMapper.key(value)) {
            case "l-methionine-sulfone" -> "L-Methionine sulfone";
            case "paracetamol" -> "Paracetamol";
            default -> value.trim();
        };
    }
    private double required(Double value, String field) {
        if (value == null || !Double.isFinite(value) || value <= 0) throw LegacyOptionsMapper.bad(field + " must be finite and positive");
        return value;
    }
    private String alphabet(LegacyCeRequest input) { return options.alphabet(input.getChemicalAlphabet(), input.getDeuterium()).name(); }
    public CemsSearchRequestDTO effmob(LegacyCeRequest input) {
        var target = new CemsSearchRequestDTO();
        target.setMzValues(masses(input));
        target.setEffectiveMobilities(input.getEffectiveMobilities());
        target.setMzTolerance(options.tolerance(input.getTolerance(), input.getToleranceMode()));
        target.setMzToleranceMode(input.getToleranceMode());
        target.setBufferCode(buffer(input));
        target.setEffectiveMobilityTolerance(required(input.getEffMobTolerance(), "eff_mob_tolerance"));
        target.setEffMobToleranceMode(input.getEffMobToleranceMode());
        target.setIonizationMode(input.getIonMode());
        target.setTemperature(temperature(input));
        target.setPolarity(input.getPolarity());
        target.setChemicalAlphabet(alphabet(input));
        target.setAdducts(new ArrayList<>(options.adducts(input)));
        return options.validated(target);
    }
    public CemsRmtSearchRequestDTO rmt(LegacyCeRequest input) {
        var target = new CemsRmtSearchRequestDTO();
        target.setMasses(masses(input));
        target.setTolerance(options.tolerance(input.getTolerance(), input.getToleranceMode()));
        target.setToleranceMode(input.getToleranceMode());
        target.setBufferCode(buffer(input));
        target.setIonMode(input.getIonMode());
        target.setRelativeMigrationTimes(input.getRmt());
        target.setRmtTolerance(required(input.getRmtTolerance(), "rmt_tolerance"));
        target.setRmtToleranceMode(input.getRmtToleranceMode());
        target.setRmtReference(reference(input.getRmtReference()));
        target.setTemperature(temperature(input));
        target.setPolarity(input.getPolarity());
        target.setChemicalAlphabet(alphabet(input));
        target.setAdducts(new ArrayList<>(options.adducts(input)));
        return options.validated(target);
    }
    public CemsMarkersRequestDTO mt1(LegacyCeRequest input) {
        var target = new CemsMarkersRequestDTO();
        target.setMasses(masses(input));
        target.setTolerance(options.tolerance(input.getTolerance(), input.getToleranceMode()));
        target.setToleranceMode(input.getToleranceMode());
        target.setBuffer(buffer(input));
        target.setIonMode(input.getIonMode());
        target.setMigrationTimes(input.getMt());
        target.setMigrationTimeTolerance(required(input.getMtTolerance(), "mt_tolerance"));
        target.setMtToleranceMode(input.getMtToleranceMode());
        target.setMarker(reference(input.getMarker()));
        target.setMarkerTime(required(input.getMarkerTime(), "marker_time"));
        target.setCapillaryLength(required(input.getCapillaryLength(), "capillary_length"));
        target.setCapillaryVoltage(required(input.getCapillaryVoltage(), "capillary_voltage"));
        target.setTemperature(temperature(input));
        target.setPolarity(input.getPolarity());
        target.setChemicalAlphabet(alphabet(input));
        target.setAdducts(new ArrayList<>(options.adducts(input)));
        return options.validated(target);
    }
    public CemsMarkersTwoRequestDTO mt2(LegacyCeRequest input) {
        var target = new CemsMarkersTwoRequestDTO();
        target.setMasses(masses(input));
        target.setTolerance(options.tolerance(input.getTolerance(), input.getToleranceMode()));
        target.setToleranceMode(input.getToleranceMode());
        target.setBuffer(buffer(input));
        target.setIonMode(input.getIonMode());
        target.setMigrationTimes(input.getMt());
        target.setMigrationTimeTolerance(required(input.getMtTolerance(), "mt_tolerance"));
        target.setMtToleranceMode(input.getMtToleranceMode());
        target.setMarker1(reference(input.getMarker()));
        target.setMarker1Time(required(input.getMarkerTime(), "marker_time"));
        target.setMarker2(reference(input.getMarker2()));
        target.setMarker2Time(required(input.getMarker2Time(), "marker2_time"));
        target.setTemperature(temperature(input));
        target.setPolarity(input.getPolarity());
        target.setChemicalAlphabet(alphabet(input));
        target.setAdducts(new ArrayList<>(options.adducts(input)));
        return options.validated(target);
    }
    public CemsRmtMarkersRequestDTO rmt1(LegacyCeRequest input) {
        var target = new CemsRmtMarkersRequestDTO();
        target.setMasses(masses(input));
        target.setTolerance(options.tolerance(input.getTolerance(), input.getToleranceMode()));
        target.setToleranceMode(input.getToleranceMode());
        target.setBuffer(buffer(input));
        target.setIonMode(input.getIonMode());
        target.setRelativeMigrationTimes(input.getRmt());
        target.setRmtTolerance(required(input.getRmtTolerance(), "rmt_tolerance"));
        target.setRmtToleranceMode(input.getRmtToleranceMode());
        target.setRmtReference(reference(input.getRmtReference()));
        target.setMarker(reference(input.getMarker()));
        target.setMarkerTime(required(input.getMarkerTime(), "marker_time"));
        target.setCapillaryLength(required(input.getCapillaryLength(), "capillary_length"));
        target.setCapillaryVoltage(required(input.getCapillaryVoltage(), "capillary_voltage"));
        target.setTemperature(temperature(input));
        target.setPolarity(input.getPolarity());
        target.setChemicalAlphabet(alphabet(input));
        target.setAdducts(new ArrayList<>(options.adducts(input)));
        return options.validated(target);
    }
    public CemsRmtMarkersTwoRequestDTO rmt2(LegacyCeRequest input) {
        var target = new CemsRmtMarkersTwoRequestDTO();
        target.setMasses(masses(input));
        target.setTolerance(options.tolerance(input.getTolerance(), input.getToleranceMode()));
        target.setToleranceMode(input.getToleranceMode());
        target.setBuffer(buffer(input));
        target.setIonMode(input.getIonMode());
        target.setRelativeMigrationTimes(input.getRmt());
        target.setRmtTolerance(required(input.getRmtTolerance(), "rmt_tolerance"));
        target.setRmtToleranceMode(input.getRmtToleranceMode());
        target.setRmtReference(reference(input.getRmtReference()));
        target.setMarker1(reference(input.getMarker()));
        target.setMarker1Time(required(input.getMarkerTime(), "marker_time"));
        target.setMarker2(reference(input.getMarker2()));
        target.setMarker2Time(required(input.getMarker2Time(), "marker2_time"));
        target.setTemperature(temperature(input));
        target.setPolarity(input.getPolarity());
        target.setChemicalAlphabet(alphabet(input));
        target.setAdducts(new ArrayList<>(options.adducts(input)));
        return options.validated(target);
    }
}
