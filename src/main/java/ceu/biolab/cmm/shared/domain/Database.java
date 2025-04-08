package ceu.biolab.cmm.shared.domain;

public enum Database {
    ALL("all"),
    HMDB("hmdb"),
    LIPIDMAPS("lipidmaps"),
    KEGG("kegg"),
    INHOUSE("in-house"),
    ASPERGILLUS("aspergillus"),
    FAHFA("fahfa"),
    CHEBI("chebi"),
    PUBCHEM("pubchem"),
    NPATLAS("npatlas")
    ;


    private final String name;

    Database(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
