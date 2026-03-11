package ceu.biolab.cmm.adapters.shared.deserializer;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import ceu.biolab.cmm.adapters.shared.domain.LegacyMetaboliteType;

public class LegacyMetaboliteTypeDeserializer extends JsonDeserializer<LegacyMetaboliteType> {

    @Override
    public LegacyMetaboliteType deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText();
        if (value == null || value.isEmpty()) {
            return LegacyMetaboliteType.ALL_EXCEPT_PEPTIDES;
        }
        for (LegacyMetaboliteType type : LegacyMetaboliteType.values()) {
            if (type.getValue().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IOException("Invalid LegacyMetaboliteType: " + value);
    }
}