package ceu.biolab.cmm.legacyApi.mapper;

import ceu.biolab.cmm.legacyApi.dto.LegacyResults;
import ceu.biolab.cmm.legacyApi.dto.LegacyRows.*;
import ceu.biolab.cmm.MSMSSearch.dto.MSMSSearchResponseDTO;
import ceu.biolab.cmm.MSMSSearch.domain.SpectrumSource;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.compound.*;
import ceu.biolab.cmm.shared.domain.msFeature.*;
import ceu.biolab.cmm.shared.service.adduct.AdductService;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class LegacyResponseMapper {
    private final LegacyOptionsMapper options;
    public LegacyResponseMapper(LegacyOptionsMapper options) { this.options = options; }

    public CompoundRow compound(Compound c) {
        CMMCompound db = c instanceof CMMCompound v ? v : null;
        String kegg = text(db == null ? null : db.getKeggID());
        String hmdb = text(db == null ? null : db.getHmdbID());
        String lm = text(db == null ? null : db.getLmID());
        String pc = text(db == null ? null : db.getPcID());
        var pathways = c.getPathways() == null ? List.<LegacyResults.Pathway>of() : c.getPathways().stream()
                .sorted(Comparator.comparing(Pathway::getPathwayName).thenComparingInt(Pathway::getPathwayId))
                .map(p -> new LegacyResults.Pathway(p.getPathwayName(), pathwayUri(p.getPathwayMap()))).toList();
        return new CompoundRow(c.getCompoundId(), c.getCompoundName(), c.getFormula(), c.getMass(), c.getCasId(), c.getInchiKey(),
                kegg, uri(kegg,"https://www.kegg.jp/entry/"), hmdb, uri(hmdb,"https://hmdb.ca/metabolites/"),
                lm, uri(lm,"https://www.lipidmaps.org/databases/lmissd/"), pc, uri(pc,"https://pubchem.ncbi.nlm.nih.gov/compound/"), "", "", pathways);
    }
    private String text(Object value) { return value == null ? "" : value.toString(); }
    private String uri(String id, String base) { return id.isEmpty() ? "" : base + id; }
    private String pathwayUri(String map) {
        if (map == null || map.isBlank()) return "";
        return map.startsWith("https://") || map.startsWith("http://") ? map : "https://www.kegg.jp/pathway/" + map;
    }
    public LegacyResults annotated(List<AnnotatedFeature> features, IonizationMode mode, boolean advanced) {
        List<Object> rows = new ArrayList<>();
        for (var feature : features) for (var group : feature.getAnnotationsByAdducts()) for (var annotation : group.getAnnotations()) {
            var c = annotation.getCompound();
            double target = AdductService.neutralMassFromMz(feature.getFeature().getMzValue(), AdductService.requireDefinition(mode, group.getAdduct()));
            var row = new AnnotationRow(compound(c), feature.getFeature().getMzValue(), options.legacyAdduct(group.getAdduct(), mode),
                    ppm(target,c.getMass()), score(annotation,"ionization"), score(annotation,"final"));
            rows.add(advanced ? new AdvancedRow(row, ((ILCMSFeature)feature.getFeature()).getRtValue(), score(annotation,"adduct"), score(annotation,"rt")) : row);
        }
        return new LegacyResults(rows);
    }
    private double score(Annotation annotation, String name) {
        for (var scores : annotation.getScores()) {
            String value = scores.getScores().get(name);
            if (value != null && !value.isBlank()) return Double.parseDouble(value) * 2d;
        }
        return -2d;
    }
    public Integer ppm(double target, double theoretical) {
        return theoretical <= 0 ? null : Math.toIntExact(Math.round(Math.abs((target-theoretical)/theoretical*1e6)));
    }
    public LegacyResults msms(MSMSSearchResponseDTO response) {
        List<MsmsRow> rows = new ArrayList<>();
        for (var annotation : response.getMsmsList()) {
            var c = annotation.getCompound();
            String source = annotation.getSpectrumSource() == null ? null : annotation.getSpectrumSource() == SpectrumSource.PREDICTED ? "Predicted" : "Experimental";
            String hmdb = c instanceof CMMCompound db ? text(db.getHmdbID()) : "";
            rows.add(new MsmsRow(source,c.getCompoundId(),hmdb,uri(hmdb,"https://hmdb.ca/metabolites/"),c.getCompoundName(),c.getFormula(),c.getMass(),annotation.getMsmsCosineScore()));
        }
        return new LegacyResults(rows);
    }
}
