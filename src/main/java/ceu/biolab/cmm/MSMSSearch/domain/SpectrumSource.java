package ceu.biolab.cmm.MSMSSearch.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SpectrumSource {
    ALL(null, "ALL"),
    EXPERIMENTAL(0L, "experimental"),
    PREDICTED(1L, "predicted");

    private final Long predictedFlag;
    private final String jsonValue;

    SpectrumSource(Long predictedFlag, String jsonValue) {
        this.predictedFlag = predictedFlag;
        this.jsonValue = jsonValue;
    }

    @JsonValue
    public String getJsonValue() {
        return jsonValue;
    }

    @JsonCreator
    public static SpectrumSource fromValue(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        for (SpectrumSource source : values()) {
            if (source.jsonValue.equalsIgnoreCase(normalized) || source.name().equalsIgnoreCase(normalized)) {
                return source;
            }
        }
        throw new IllegalArgumentException("Unknown MS/MS spectrum source: " + value);
    }

    public static SpectrumSource fromPredictedFlag(Long predictedFlag) {
        return Long.valueOf(1L).equals(predictedFlag) ? PREDICTED : EXPERIMENTAL;
    }

    public String sqlFilterClause() {
        return switch (this) {
            case ALL -> "";
            case EXPERIMENTAL -> "AND predicted = 0";
            case PREDICTED -> "AND predicted = 1";
        };
    }
}
