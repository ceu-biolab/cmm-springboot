package ceu.biolab.cmm.shared.domain;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import ceu.biolab.cmm.shared.domain.MzToleranceMode;

public class MzToleranceModeDeserializer extends JsonDeserializer<MzToleranceMode> {

    @Override
    public MzToleranceMode deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText();
        if (value == null || value.isEmpty()) {
            return MzToleranceMode.PPM; // Default fallback
        }
        try {
            return MzToleranceMode.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid MzToleranceMode: " + value);
        }
    }
}