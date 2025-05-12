package ceu.biolab.cmm.browseSearch.dto;

import ceu.biolab.cmm.shared.domain.Database;
import ceu.biolab.cmm.shared.domain.MetaboliteType;

import java.util.ArrayList;
import java.util.List;


public class BrowseSearchRequest {
    private String compound_name;
    private String formula;
    private List<Database> databases;
    private MetaboliteType metaboliteType;
    private boolean exact_name;

    public BrowseSearchRequest(String searchTerm, String searchFormula, List<Database> databases, MetaboliteType metaboliteType, boolean exact_name) {
        if(searchTerm == null || searchTerm.isEmpty()) this.compound_name = "";
        else this.compound_name = searchTerm;
        if(searchFormula != null && !searchFormula.isEmpty()) this.formula = "";
        else this.formula = searchFormula;
        this.databases = databases;
        this.metaboliteType = metaboliteType;
        this.exact_name = exact_name;
    }

    public BrowseSearchRequest() {
    this.compound_name = "";
    this.formula = "";
    this.databases = new ArrayList<>();
    this.metaboliteType = MetaboliteType.ALL;
    this.exact_name = false;
        }

    public String getCompound_name() {return compound_name;}
    public void setCompound_name(String compound_name) {if(compound_name == null || compound_name.isEmpty()) this.compound_name = "";
    else this.compound_name = compound_name;}

    public String getFormula() {return formula;}
    public void setFormula(String formula) {if(formula == null || formula.isEmpty()) this.formula = "";
    else this.formula = formula;}

    public List<Database> getDatabases() {return databases;}

    public void setDatabases(List<Database> databases) {this.databases = databases;}

    public MetaboliteType getMetaboliteType() {return metaboliteType;}
    public void setMetaboliteType(MetaboliteType metaboliteType) {this.metaboliteType = metaboliteType;}

    public boolean isExact_name() {
        return exact_name;
    }

    public void setExact_name(boolean exact_name) {
        this.exact_name = exact_name;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return "BrowseSearchRequest{" +
                "searchTerm='" + compound_name + '\'' +
                ", searchFormula='" + formula + '\'' +
                ", databases=" + databases +
                ", metaboliteType=" + metaboliteType +
                '}';
    }
}
