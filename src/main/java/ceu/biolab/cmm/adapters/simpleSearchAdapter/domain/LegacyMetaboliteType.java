package ceu.biolab.cmm.adapters.simpleSearchAdapter.domain;

public enum LegacyMetaboliteType {
 

    ALL_EXCEPT_PEPTIDES("all-except-peptides"),
    ONLY_LIPIDS("only-lipids"),
    ALL_INCLUDING_PEPTIDES("all-including-peptides");

    private final String value;

    LegacyMetaboliteType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
