package ceu.biolab.cmm.legacyApi.mapper;

import ceu.biolab.cmm.compoundDetails.dto.*;
import ceu.biolab.cmm.legacyApi.dto.LegacyDetailsRows.*;
import ceu.biolab.cmm.msSearch.domain.compound.LipidMapsClassification;
import ceu.biolab.cmm.shared.domain.compound.CMMCompound;
import org.springframework.stereotype.Component;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;

@Component
public class LegacyDetailsMapper {
    public ClassyfireNode classyfire(ClassyfireNodeResponse value) {
        return new ClassyfireNode(value.nodeName(), shortId(value.nodeId()), shortId(value.kingdom()), shortId(value.superClass()),
                shortId(value.mainClass()), shortId(value.subClass()), shortId(value.directParent()), shortId(value.level7()),
                shortId(value.level8()), shortId(value.level9()), shortId(value.level10()), null, date(value.created()), date(value.lastUpdated()));
    }
    public OntologyTerm ontology(OntologyTermResponse value) {
        return new OntologyTerm(value.ontologyTermId(),value.term(),value.definition(),value.externalId(),value.externalSource(),
                value.ontologyComment(),value.curator(),value.parentId(),value.ontologyLevel(),date(value.created()),date(value.lastUpdated()));
    }
    public Reference reference(ReferenceResponse value) {
        return new Reference(value.referenceId(),value.referenceText(),value.doi(),value.link(),links(value.compounds()),
                links(value.organisms()),date(value.created()),date(value.lastUpdated()));
    }
    public Organism organism(OrganismResponse value) {
        return new Organism(value.organismId(),value.organismName(),value.parentId(),value.organismLevel(),
                value.organismHierarchy().stream().map(v -> new NumberedLink(v.id(),v.name(),v.level())).toList(),
                links(value.compounds()),references(value.references()),date(value.created()),date(value.lastUpdated()));
    }
    public CompoundInfo compound(CompoundDetailsResponse value) {
        var c = value.compound();
        CMMCompound ids = c instanceof CMMCompound v ? v : null;
        var classifications = c.getLipidMapsClassifications();
        LipidMapsClassification lipid = classifications == null ? null : classifications.stream().sorted(
                Comparator.comparing(LipidMapsClassification::getCategory,Comparator.nullsFirst(Comparator.naturalOrder()))
                        .thenComparing(LipidMapsClassification::getMainClass,Comparator.nullsFirst(Comparator.naturalOrder()))
                        .thenComparing(LipidMapsClassification::getSubClass,Comparator.nullsFirst(Comparator.naturalOrder()))
                        .thenComparing(LipidMapsClassification::getClassLevel4,Comparator.nullsFirst(Comparator.naturalOrder())))
                .findFirst().orElse(null);
        String charge = switch(c.getChargeType()) { case 1 -> "positive"; case 2 -> "negative"; default -> "neutral"; };
        return new CompoundInfo(c.getCompoundId(),c.getCasId(),c.getCompoundName(),c.getFormula(),c.getMass(),charge,c.getChargeNumber(),
                c.getLogP(),ids == null ? null : ids.getKeggID(),ids == null ? null : ids.getLmID(),ids == null ? null : ids.getHmdbID(),
                null,ids == null ? null : ids.getPcID(),ids == null ? null : ids.getAspergillusWebName(),ids == null ? null : ids.getChebiID(),
                ids == null ? null : ids.getAspergillusID(),ids == null ? null : ids.getKnapsackID(),ids == null ? null : ids.getNpatlasID(),
                ids == null ? null : ids.getOhPositionID(),c.getBiologicalActivity(),c.getMeshNomenclature(),c.getIupacClassification(),
                c.getInchi(),c.getInchiKey(),c.getSmiles(),c.getNumChains(),c.getNumCarbons(),c.getDoubleBonds(),
                lipid == null ? null : lipid.getCategory(),lipid == null ? null : lipid.getMainClass(),
                lipid == null ? null : lipid.getSubClass(),lipid == null ? null : lipid.getClassLevel4(),
                links(value.reactions()),value.classyfireClassification().stream().map(v -> new Link(shortId(v.id()),v.name())).toList(),
                links(value.ontologyTerms()),references(value.references()),links(value.organisms()),date(value.created()),date(value.lastUpdated()));
    }
    private List<Link> links(List<EntityLink> values) { return values.stream().map(v -> new Link(v.id(),v.name())).toList(); }
    private List<Link> references(List<EntityLink> values) {
        return values.stream().map(v -> new Link(v.uri() == null || v.uri().isBlank() ? "/api/v3/references/"+v.id() : v.uri(),v.name())).toList();
    }
    private String date(OffsetDateTime value) { return value == null ? null : value.toLocalDate().toString(); }
    private String shortId(String value) { return value == null ? null : value.replaceFirst("(?i)^CHEMONTID:",""); }
}
