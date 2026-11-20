package ceu.biolab.cmm.legacyApi.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import java.util.List;

/** Closed response contracts, independent of the current backend's JSON models. */
public final class LegacyRows {
    private LegacyRows() {}
    public record CompoundRow(int identifier, String name, String formula, double molecular_weight,
            String cas, String inChiKey, String kegg_compound, String kegg_uri, String hmdb_compound,
            String hmdb_uri, String lipidmaps_compound, String lipidmaps_uri, String pubchem_compound,
            String pubchem_uri, String metlin_compound, String metlin_uri, List<LegacyResults.Pathway> pathways) {}
    public record AnnotationRow(@JsonUnwrapped CompoundRow compound, double EM, String adduct,
            Integer error_ppm, double ionizationScore, double finalScore) {}
    public record AdvancedRow(@JsonUnwrapped AnnotationRow annotation, double RT, double adductRelationScore, double RTscore) {}
    public record MsmsRow(String spectral_display_tools, int identifier, String hmdb_compound, String hmdb_uri,
            String name, String formula, double mass, Double score) {}
    public record CeCompoundRow(int id, String name, String formula, double molecular_weight, Long error_ppm,
            String cas_id, String hmdb_id, String hmdb_uri, String metlin_id, String metlin_uri,
            String lipidmaps_id, String lipidmaps_uri, String kegg_id, String kegg_uri,
            String pubchem_id, String pubchem_uri, String chebi_id, String chebi_uri, List<LegacyResults.Pathway> pathways) {}
    public record EffMobAnnotation(@JsonUnwrapped CeCompoundRow compound, Double effmob, Long error_effmob) {}
    public record RmtAnnotation(@JsonUnwrapped CeCompoundRow compound, Double rmt, Long error_rmt) {}
    public record MtAnnotation(@JsonUnwrapped CeCompoundRow compound, Double predicted_mt, Long error_mt) {}
    public record AdductGroup(String adduct, List<?> compounds) {}
    public record EffMobGroup(double input_mz, double input_effmob, List<AdductGroup> annotations) {}
    public record RmtGroup(double input_mz, double input_rmt, List<AdductGroup> annotations) {}
    public record MtGroup(double input_mz, double input_mt, List<AdductGroup> annotations) {}
}
