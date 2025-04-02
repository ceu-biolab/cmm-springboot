package ceu.biolab.cmm.scoreAnnotations.model;

import org.drools.ruleunits.api.DataSource;
import org.drools.ruleunits.api.DataStore;
import org.drools.ruleunits.api.RuleUnitData;

public class LipidsUnit implements RuleUnitData {
    
    private final DataStore<EvaluatedLipid> compounds;
    
    public LipidsUnit() {
        this(DataSource.createStore());
    }

    public LipidsUnit(DataStore<EvaluatedLipid> compounds) {
        this.compounds = compounds;
    }
    
    public DataStore<EvaluatedLipid> getCompounds() {
        return compounds;
    }
}
