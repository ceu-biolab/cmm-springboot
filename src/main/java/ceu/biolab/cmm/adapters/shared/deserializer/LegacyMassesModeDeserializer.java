package ceu.biolab.cmm.adapters.shared.deserializer;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import ceu.biolab.cmm.adapters.shared.domain.LegacyMassesMode;

public class LegacyMassesModeDeserializer extends JsonDeserializer<LegacyMassesMode> {
    @Override
    public LegacyMassesMode deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText();
        if (value == null || value.isEmpty()) {
            return LegacyMassesMode.MZ;
        }
        for (LegacyMassesMode mode : LegacyMassesMode.values()) {
            if (mode.getValue().equalsIgnoreCase(value)) {
                return mode;
            }
        }
        throw new IOException("Invalid LegacyMassesMode: " + value);
    }
}
