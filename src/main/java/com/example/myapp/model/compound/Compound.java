package com.example.myapp.model.compounds;

public class Compound {
    private final Integer compound_id;
    private final Double mass;
    private final String formula;
    private final String compound_name;
    private final String cas_id;
    private final Integer formula_type;
    private final Integer compound_type;
    private final Integer compound_status;
    private final Integer charge_type;
    private final Integer charge_number;

    private final Structure structure;
    // Several compounds have one LM_Classification
    private final LM_Classification lm_classification;
    // ClassyFire is a web-based application for automated structural classification of chemical entities.
    // Many compounds have many classyfire_classifications (many to many relationship)
    private final List<Classyfire_Classification> classsyfire_classification;
    // Many compounds have one lipids_classidication (duda alberto)
    private final Lipids_Classification lipids_classification;
    // List of pathways in wich the compound is involved. Several compounds can have several pathways (many to many relationship)
    private final List<Pathway> pathways;



    
}
