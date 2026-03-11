package ceu.biolab.cmm.adapters.shared.translators;

import java.util.HashSet;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import ceu.biolab.cmm.adapters.shared.domain.LegacyDatabase;
import ceu.biolab.cmm.shared.domain.Database;

public final class DatabaseTranslation {

    private DatabaseTranslation() {
    }

    public static LegacyDatabase toLegacyDatabase(String value) {
        if (value == null) return null;
        String normalized = value.trim().toLowerCase();
        switch (normalized) {
            case "all":
                return LegacyDatabase.ALL;
            case "hmdb":
                return LegacyDatabase.HMDB;
            case "metlin":
                return LegacyDatabase.METLIN;
            case "in_house":
            case "in-house":
                return LegacyDatabase.IN_HOUSE;
            case "kegg":
                return LegacyDatabase.KEGG;
            case "lipidsmaps":
            case "lipidmaps":
                return LegacyDatabase.LIPIDSMAPS;
            case "mine":
                return LegacyDatabase.MINE;
            default:
                // Try to match by enum name, replacing '-' with '_'
                String enumName = value.trim().toUpperCase().replace("-", "_");
                try {
                    return LegacyDatabase.valueOf(enumName);
                } catch (IllegalArgumentException e) {
                    return null;
                }
        }
    }

    public static Set<Database> toDatabases(Set<LegacyDatabase> legacyDatabases) {
        Set<Database> databases = new HashSet<>();
        try {
            for (LegacyDatabase db : legacyDatabases) {
                String key = db.getValue().toUpperCase().replace("-", "");
                databases.add(Database.valueOf(key));
            }
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.GONE, "One or more selected databases are no longer supported");
        }
        return databases;
    }
}
