package com.example.myapp.model;

public class ParserJSON {

    public static MetaboliteType parseMetaboliteType(String value) {
        if (value == null) return null;
        if (value.equalsIgnoreCase("All except peptides")) {
            return MetaboliteType.ALLEXCEPTPEPTIDES;
        }
        if (value.equalsIgnoreCase("Only lipids")) {
            return MetaboliteType.ONLYLIPIDS;
        }
        throw new IllegalArgumentException("Unknown metabolite type: " + value);
    }


    public static ToleranceMode parseToleranceMode(String value) {
        if (value == null) return null;
        if (value.equalsIgnoreCase("ppm")) {
            return ToleranceMode.PPM;
        }
        if (value.equalsIgnoreCase("mDa")) {
            return ToleranceMode.DA;
        }
        throw new IllegalArgumentException("Incorrect tolerance unit: " + value);
    }

    public static Databases parseDatabases(String value) {
        if (value == null) return null;
        if (value.equalsIgnoreCase("All")) {
            return Databases.ALL;
        }
        if (value.equalsIgnoreCase("LipidMaps")) {
            return Databases.LIPIDMAPS;
        }
        if (value.equalsIgnoreCase("Kegg")) {
            return Databases.KEGG;
        }
        if (value.equalsIgnoreCase("Aspergillus")) {
            return Databases.ASPERGILLUS;
        }
        if (value.equalsIgnoreCase("FAHFA Lipids")) {
            return Databases.FAHFALIPIDS;
        }
        if (value.equalsIgnoreCase("Metlin")) {
            return Databases.METLIN;
        }
        if (value.equalsIgnoreCase("HMDB")) {
            return Databases.HMDB;
        }
        throw new IllegalArgumentException("Unknown database: " + value);
    }


    public static IonizationMode parseIonizationMode(String value) {
        if (value == null) return null;
        if (value.equalsIgnoreCase("Neutral")) {
            return IonizationMode.NEUTRAL;
        }
        if (value.equalsIgnoreCase("Positive Mode")) {
            return IonizationMode.POSITIVE;
        }
        if (value.equalsIgnoreCase("Negative Mode")) {
            return IonizationMode.NEGATIVE;
        }
        throw new IllegalArgumentException("Incorrect Ionization mode: " + value);
    }
}
