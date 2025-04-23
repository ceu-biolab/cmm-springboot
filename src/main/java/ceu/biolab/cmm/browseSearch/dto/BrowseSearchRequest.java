package ceu.biolab.cmm.browseSearch.dto;

import ceu.biolab.cmm.shared.domain.Database;
import ceu.biolab.cmm.shared.domain.MetaboliteType;

import java.util.List;


public class BrowseSearchRequest {
    private String compoundName;
    private String compoundFormula;
    private List<Database> databases;
    private MetaboliteType metaboliteType;

    public BrowseSearchRequest(String searchTerm, String searchFormula, List<Database> databases, MetaboliteType metaboliteType) {
        this.compoundName = searchTerm;
        this.compoundFormula = searchFormula;
        this.databases = databases;
        this.metaboliteType = metaboliteType;
    }

    public BrowseSearchRequest() {
    }

    public String getCompoundName() {return compoundName;}
    public void setCompoundName(String compoundName) {this.compoundName = compoundName;}

    public String getCompoundFormula() {return compoundFormula;}
    public void setCompoundFormula(String compoundFormula) {this.compoundFormula = compoundFormula;}

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
                "searchTerm='" + compoundName + '\'' +
                ", searchFormula='" + compoundFormula + '\'' +
                ", databases=" + databases +
                ", metaboliteType=" + metaboliteType +
                '}';
    }
}
