package ceu.biolab.cmm.shared.domain;

public enum MetaboliteType {
    //* All : metabolites and lipids
    ALL("all"),

    //* Only lipids (compound_type = 1)
    ONLYLIPIDS("only-lipids"),
    ;

    private final String name;

    MetaboliteType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
