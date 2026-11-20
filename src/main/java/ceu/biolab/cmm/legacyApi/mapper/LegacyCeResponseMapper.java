package ceu.biolab.cmm.legacyApi.mapper;

import ceu.biolab.cmm.legacyApi.dto.*;
import ceu.biolab.cmm.legacyApi.dto.LegacyRows.*;
import ceu.biolab.cmm.CEMSSearch.dto.*;
import ceu.biolab.cmm.shared.domain.compound.CMMCompound;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class LegacyCeResponseMapper {
    private final LegacyResponseMapper compounds;
    private final LegacyOptionsMapper options;
    public LegacyCeResponseMapper(LegacyResponseMapper compounds, LegacyOptionsMapper options) { this.compounds = compounds; this.options = options; }
    public LegacyResults.CeResults map(CemsSearchResponseDTO response, LegacyCeRequest input, String route) {
        boolean eff = route.equals("effmob"), mt = route.startsWith("mt");
        List<Double> values = eff ? input.getEffectiveMobilities() : mt ? input.getMt() : input.getRmt();
        List<Object> rows = new ArrayList<>();
        if (response.getCeFeatures().size() != input.getMasses().size()) throw new IllegalStateException("CE service changed feature cardinality");
        for (int i = 0; i < response.getCeFeatures().size(); i++) {
            var feature = response.getCeFeatures().get(i);
            List<AdductGroup> groups = new ArrayList<>();
            for (var group : feature.getAnnotationsByAdducts()) {
                List<Object> annotations = new ArrayList<>();
                for (var annotation : group.getAnnotations()) {
                    var c = annotation.getCompound(); var base = compounds.compound(c);
                    var db = c instanceof CMMCompound v ? v : null;
                    String chebi = db == null || db.getChebiID() == null ? "" : db.getChebiID().toString();
                    var row = new CeCompoundRow(c.getCompoundId(),base.name(),base.formula(),base.molecular_weight(),rounded(annotation.getMassErrorPpm()),
                            base.cas(),base.hmdb_compound(),base.hmdb_uri(),base.metlin_compound(),base.metlin_uri(),base.lipidmaps_compound(),base.lipidmaps_uri(),
                            base.kegg_compound(),base.kegg_uri(),base.pubchem_compound(),base.pubchem_uri(),chebi,
                            chebi.isEmpty() ? "" : "https://www.ebi.ac.uk/chebi/searchId.do?chebiId=CHEBI:"+chebi,base.pathways());
                    if (eff) {
                        Double mobility = annotation.getMobilityErrorPct() == null ? null : feature.getFeature().getEffectiveMobility()*(1+annotation.getMobilityErrorPct()/100d);
                        annotations.add(new EffMobAnnotation(row,mobility,rounded(annotation.getMobilityErrorPct())));
                    } else if (mt) annotations.add(new MtAnnotation(row,annotation.getAbsoluteMt(),null));
                    else annotations.add(new RmtAnnotation(row,annotation.getRelativeMt(),rounded(annotation.getRmtErrorPct())));
                }
                groups.add(new AdductGroup(options.legacyAdduct(group.getAdduct(),options.ionMode(input.getIonMode())),annotations));
            }
            double mz = feature.getFeature().getMzValue(); double value = values.get(i);
            rows.add(eff ? new EffMobGroup(mz,value,groups) : mt ? new MtGroup(mz,value,groups) : new RmtGroup(mz,value,groups));
        }
        return new LegacyResults.CeResults(rows,List.of());
    }
    private Long rounded(Double value) { return value == null ? null : Math.round(Math.abs(value)); }
}
