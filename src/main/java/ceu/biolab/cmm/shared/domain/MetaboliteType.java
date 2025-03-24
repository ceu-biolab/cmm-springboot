package ceu.biolab.cmm.shared.domain;

public enum MetaboliteType {
    NOPEPTIDES("all-except-peptides"), 
    ONLYLIPIDS("only-lipids"),
    ALL("all-including-peptides")
    ;

    private final String name;

    MetaboliteType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
