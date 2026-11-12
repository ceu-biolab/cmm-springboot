package ceu.biolab.cmm.browseSearch.dto;

import ceu.biolab.cmm.shared.domain.Database;
import ceu.biolab.cmm.shared.domain.FormulaType;
import ceu.biolab.cmm.shared.domain.MetaboliteType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.HashSet;
import java.util.Set;

@Data
public class BrowseSearchRequest {
    private String compoundName;
    private String formula;
    @NotEmpty
    private Set<@NotNull Database> databases;
    @NotNull
    private MetaboliteType metaboliteType;
    private boolean exactName;
    private FormulaType formulaType;

    public BrowseSearchRequest(String searchTerm, String searchFormula, Set<Database> databases, MetaboliteType metaboliteType, boolean exactName) {
        this.compoundName = (searchTerm == null || searchTerm.isEmpty()) ? "" : searchTerm;
        this.formula = (searchFormula == null || searchFormula.isEmpty()) ? "" : searchFormula;
        this.databases = databases;
        this.metaboliteType = metaboliteType;
        this.exactName = exactName;
        this.formulaType = null;
    }

    public BrowseSearchRequest(String searchTerm,
                               String searchFormula,
                               Set<Database> databases,
                               MetaboliteType metaboliteType,
                               boolean exactName,
                               FormulaType formulaType) {
        this(searchTerm, searchFormula, databases, metaboliteType, exactName);
        this.formulaType = formulaType;
    }

    public BrowseSearchRequest() {
        this.compoundName = "";
        this.formula = "";
        this.databases = new HashSet<>();
        this.metaboliteType = MetaboliteType.ALL;
        this.exactName = false;
        this.formulaType = null;
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
