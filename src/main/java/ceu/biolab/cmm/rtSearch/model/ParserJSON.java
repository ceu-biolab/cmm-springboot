package ceu.biolab.cmm.rtSearch.model;

import ceu.biolab.cmm.shared.domain.Database;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;

public class ParserJSON {

    /*public static MetaboliteType parseMetaboliteType(String value) {
        if (value == null) return null;
        if (value.equalsIgnoreCase("All except peptides")) {
            return MetaboliteType.ALLEXCEPTPEPTIDES;
        }
        if (value.equalsIgnoreCase("Only lipids")) {
            return MetaboliteType.ONLYLIPIDS;
        }
        throw new IllegalArgumentException("Unknown metabolite type: " + value);
    }
     */


    public static MzToleranceMode parseToleranceMode(String value) {
        if (value == null) return null;
        if (value.equalsIgnoreCase("ppm")) {
            return MzToleranceMode.PPM;
        }
        if (value.equalsIgnoreCase("mDa")) {
            return MzToleranceMode.MDA;
        }
        throw new IllegalArgumentException("Incorrect tolerance unit: " + value);
    }

    public static Database parseDatabases(String value) {
        if (value == null) return null;
        if (value.equalsIgnoreCase("All")) {
            return Database.ALL;
        }
        if (value.equalsIgnoreCase("LipidMaps")) {
            return Database.LIPIDMAPS;
        }
        if (value.equalsIgnoreCase("Kegg")) {
            return Database.KEGG;
        }
        if (value.equalsIgnoreCase("Aspergillus")) {
            return Database.ASPERGILLUS;
        }
        if (value.equalsIgnoreCase("FAHFA Lipids")) {
            return Database.FAHFA;
        }
        if (value.equalsIgnoreCase("HMDB")) {
            return Database.HMDB;
        }
        throw new IllegalArgumentException("Unknown database: " + value);
    }


    public static IonizationMode parseIonizationMode(String value) {
        if (value == null) return null;
        if (value.equalsIgnoreCase("Positive Mode")) {
            return IonizationMode.POSITIVE;
        }
        if (value.equalsIgnoreCase("Negative Mode")) {
            return IonizationMode.NEGATIVE;
        }
        throw new IllegalArgumentException("Incorrect Ionization mode: " + value);
    }
}
