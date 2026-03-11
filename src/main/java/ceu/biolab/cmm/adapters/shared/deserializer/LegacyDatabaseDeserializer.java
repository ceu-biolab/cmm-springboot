package ceu.biolab.cmm.adapters.shared.deserializer;

import java.io.IOException;
 
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import ceu.biolab.cmm.adapters.shared.domain.LegacyDatabase;

public class LegacyDatabaseDeserializer extends JsonDeserializer<LegacyDatabase> {

    @Override
    public LegacyDatabase deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText();
        if (value == null || value.isEmpty()) {
            return LegacyDatabase.ALL_EXCEPT_MINE;
        }
        String normalized = value.replace('-', '_').toUpperCase();
        for (LegacyDatabase db : LegacyDatabase.values()) {
            if (db.name().equals(normalized)) {
                return db;
            }
        }
        throw new IOException("Invalid LegacyDatabase: " + value);
    }
}