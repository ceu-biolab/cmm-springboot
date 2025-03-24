package ceu.biolab.cmm.shared.domain;

public enum Database {
    KEGG("kegg"),
    HMDB("hmdb"),
    LIPIDMAPS("lipidmaps"),
    METLIN("metlin"),
    MINE("mine"),
    INHOUSE("in-house"),
    ASPERGUILLUS("asperguillus"),
    FAHFA("fahfa")
    ;

    private final String name;

    Database(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
