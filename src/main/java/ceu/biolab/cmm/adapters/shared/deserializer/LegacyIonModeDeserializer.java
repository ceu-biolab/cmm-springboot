package ceu.biolab.cmm.adapters.shared.deserializer;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import ceu.biolab.cmm.adapters.shared.domain.LegacyIonMode;

/**
 * Deserializer for LegacyIonMode accepting legacy strings
 * like "positive" and "negative" (case-insensitive).
 */
public class LegacyIonModeDeserializer extends JsonDeserializer<LegacyIonMode> {

    @Override
    public LegacyIonMode deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText();
        if (value == null || value.isEmpty()) {
            return LegacyIonMode.POSITIVE;
        }
        for (LegacyIonMode mode : LegacyIonMode.values()) {
            if (mode.getValue().equalsIgnoreCase(value) || mode.name().equalsIgnoreCase(value)) {
                return mode;
            }
        }
        throw new IOException("Invalid LegacyIonMode: " + value);
    }
}
