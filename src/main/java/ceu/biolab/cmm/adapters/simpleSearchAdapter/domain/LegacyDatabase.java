package ceu.biolab.cmm.adapters.simpleSearchAdapter.domain;

public enum LegacyDatabase {
    
    ALL("all"), 
    ALL_EXCEPT_MINE("all-except-mine"),
    HMDB("HMDB"), 
    LIPIDSMAPS("LipidMaps"),
    METLIN("Metlin"), 
    KEGG("Kegg"),
    IN_HOUSE("in-house"), 
    MINE("mine");

    private final String value;

    LegacyDatabase(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
