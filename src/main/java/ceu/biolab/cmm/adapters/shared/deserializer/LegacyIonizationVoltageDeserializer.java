package ceu.biolab.cmm.adapters.shared.deserializer;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import ceu.biolab.cmm.adapters.shared.domain.LegacyIonizationVoltage;

/**
 * Deserializer for LegacyIonizationVoltage accepting legacy strings
 * like "low", "medium", "high", "all" (case-insensitive).
 */
public class LegacyIonizationVoltageDeserializer extends JsonDeserializer<LegacyIonizationVoltage> {

    @Override
    public LegacyIonizationVoltage deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText();
        if (value == null || value.isEmpty()) {
            return LegacyIonizationVoltage.LOW;
        }
        for (LegacyIonizationVoltage v : LegacyIonizationVoltage.values()) {
            if (v.getValue().equalsIgnoreCase(value) || v.name().equalsIgnoreCase(value)) {
                return v;
            }
        }
        throw new IOException("Invalid LegacyIonizationVoltage: " + value);
    }
}
