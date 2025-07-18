package ceu.biolab.cmm.browseSearch.dto;

import ceu.biolab.cmm.shared.domain.Database;
import ceu.biolab.cmm.shared.domain.MetaboliteType;
import lombok.Data;
import java.util.HashSet;
import java.util.Set;

@Data
public class BrowseSearchRequest {
    private String compoundName;
    private String formula;
    private Set<Database> databases;
    private MetaboliteType metaboliteType;
    private boolean exactName;

    public BrowseSearchRequest(String searchTerm, String searchFormula, Set<Database> databases, MetaboliteType metaboliteType, boolean exactName) {
        if(searchTerm == null || searchTerm.isEmpty()) {
            this.compoundName = "";
        } else this.compoundName = searchTerm;
        if(searchFormula != null || !searchFormula.isEmpty()) {
            this.formula = "";
        } else this.formula = searchFormula;
        this.databases = databases;
        this.metaboliteType = metaboliteType;
        this.exactName = exactName;
    }

    public BrowseSearchRequest() {
        this.compoundName = "";
        this.formula = "";
        this.databases = new HashSet<>();
        this.metaboliteType = MetaboliteType.ALL;
        this.exactName = false;
    }

    public void setCompoundName(String compound_name) {
        if(compound_name == null || compound_name.isEmpty()) {
            this.compoundName = "";
        } else this.compoundName = compound_name;
    }

    public void setFormula(String formula) {
        if(formula == null || formula.isEmpty()) {
            this.formula = "";
        } else this.formula = formula;
    }

    public boolean isExactName() {
        return exactName;
    }
}
