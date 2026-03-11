package ceu.biolab.cmm.shared.domain;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class IonizationModeDeserializer extends JsonDeserializer<IonizationMode> {
    @Override
    public IonizationMode deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText();
        if (value == null || value.isEmpty()) {
            return IonizationMode.POSITIVE; // Default fallback
        }
        try {
            return IonizationMode.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid IonizationMode: " + value);
        }
    }
}
