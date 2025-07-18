package ceu.biolab.cmm.shared.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public enum Database {
    ALL("all"),
    HMDB("hmdb"),
    LIPIDMAPS("lipidmaps"),
    KEGG("kegg"),
    INHOUSE("in-house"),
    ASPERGILLUS("aspergillus"),
    FAHFA("fahfa"),
    CHEBI("chebi"),
    PUBCHEM("pubchem"),
    NPATLAS("npatlas")
    ;


    private final String name;

    Database(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static List<String> databaseConditions(Set<Database> databaseList){
        List<String> databasesConditionsList = new ArrayList<>();

        if (databaseList.contains(Database.HMDB)) {
            databasesConditionsList.add("c.hmdb_id IS NOT NULL");
        }
        if (databaseList.contains(Database.LIPIDMAPS)) {
            databasesConditionsList.add("c.lm_id IS NOT NULL");
        }
        if (databaseList.contains(Database.KEGG)) {
            databasesConditionsList.add("c.kegg_id IS NOT NULL");
        }
        if (databaseList.contains(Database.INHOUSE)) {
            databasesConditionsList.add("c.in_house_id IS NOT NULL");
        }
        if (databaseList.contains(Database.ASPERGILLUS)) {
            databasesConditionsList.add("c.aspergillus_id IS NOT NULL");
        }
        if (databaseList.contains(Database.FAHFA)) {
            databasesConditionsList.add("c.fahfa_id IS NOT NULL");
        }
        if (databaseList.contains(Database.CHEBI)) {
            databasesConditionsList.add("c.chebi_id IS NOT NULL");
        }
        if (databaseList.contains(Database.PUBCHEM)) {
            databasesConditionsList.add("c.pc_id IS NOT NULL");
        }
        if (databaseList.contains(Database.NPATLAS)) {
            databasesConditionsList.add("c.npatlas_id IS NOT NULL");
        }
        if (databaseList.contains(Database.ALL)) {
            databasesConditionsList.add("c.npatlas_id IS NOT NULL");
            databasesConditionsList.add("c.lm_id IS NOT NULL");
            databasesConditionsList.add("c.kegg_id IS NOT NULL");
            databasesConditionsList.add("c.in_house_id IS NOT NULL");
            databasesConditionsList.add("c.fahfa_id IS NOT NULL");
            databasesConditionsList.add("c.pc_id IS NOT NULL");
            databasesConditionsList.add("c.aspergillus_id IS NOT NULL");
            databasesConditionsList.add("c.chebi_id IS NOT NULL");
        }

        return databasesConditionsList;
    }
}
