package ceu.biolab.cmm.browseSearch.dto;

import ceu.biolab.cmm.shared.domain.Database;
import ceu.biolab.cmm.shared.domain.MetaboliteType;

import java.util.List;


public class BrowseSearchRequest {
    private String compound_name;
    private String formula;
    private List<Database> databases;
    private MetaboliteType metaboliteType;

    public BrowseSearchRequest(String searchTerm, String searchFormula, List<Database> databases, MetaboliteType metaboliteType) {
        this.compound_name = searchTerm;
        this.formula = searchFormula;
        this.databases = databases;
        this.metaboliteType = metaboliteType;
    }

    public BrowseSearchRequest() {
    }

    public String getCompound_name() {return compound_name;}
    public void setCompound_name(String compound_name) {this.compound_name = compound_name;}

    public String getFormula() {return formula;}
    public void setFormula(String formula) {this.formula = formula;}

    public List<Database> getDatabases() {return databases;}

    public void setDatabases(List<Database> databases) {this.databases = databases;}

    public MetaboliteType getMetaboliteType() {return metaboliteType;}
    public void setMetaboliteType(MetaboliteType metaboliteType) {this.metaboliteType = metaboliteType;}

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
