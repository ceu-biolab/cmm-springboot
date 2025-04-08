package ceu.biolab.cmm.shared.domain;

public enum MetaboliteType {
    //* All except peptides : metabolites (compound_type = 0)
    ALLEXCEPTPEPTIDES("all-except-peptides"),
    //* Only lipids (compound_type = 1)
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
