package ceu.biolab.cmm.adapters.shared.domain;

public enum LegacyDatabase {
    
    ALL("all"), 
    ALL_EXCEPT_MINE("all"), // In the new system we don't have MINE so ALL and ALL_EXCEPT_MINE are equivalent. 
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
