package ceu.biolab.cmm.unit.legacyApi;

import ceu.biolab.cmm.legacyApi.dto.*;
import ceu.biolab.cmm.legacyApi.mapper.*;
import ceu.biolab.cmm.MSMSSearch.dto.MSMSSearchResponseDTO;
import ceu.biolab.cmm.MSMSSearch.domain.*;
import ceu.biolab.cmm.CEMSSearch.dto.*;
import ceu.biolab.cmm.scoreAnnotations.domain.CompoundScores;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.compound.*;
import ceu.biolab.cmm.shared.domain.msFeature.*;
import com.fasterxml.jackson.databind.*;
import jakarta.validation.Validation;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class LegacyResponseMapperTest {
    private static jakarta.validation.ValidatorFactory factory;
    private LegacyOptionsMapper options;
    private LegacyResponseMapper mapper;
    private final ObjectMapper json = Jackson2ObjectMapperBuilder.json().build();
    @BeforeAll static void factory() { factory = Validation.buildDefaultValidatorFactory(); }
    @AfterAll static void closeFactory() { factory.close(); }
    @BeforeEach void setup() { options = new LegacyOptionsMapper(factory.getValidator()); mapper = new LegacyResponseMapper(options); }
    static CMMCompound compound() {
        return CMMCompound.builder().compoundId(42).compoundName("water").formula("H2O").mass(100d).compoundType(CompoundType.NON_LIPID)
                .pathways(Set.of(new Pathway(1,"path","https://example.test/path"))).hmdbID("HMDB1").pcID(123).build();
    }
    static AnnotatedFeature feature(double mz, double rt, String adduct) {
        var feature = new AnnotatedFeature(new LCMSFeature(rt, mz));
        feature.addCompoundForAdduct(adduct, compound());
        return feature;
    }
    @Test void simpleResponseMatchesIndependentLegacyContract() throws Exception {
        var actual=json.valueToTree(mapper.annotated(List.of(feature(101.007276,2,"[M+H]+")),IonizationMode.POSITIVE,false));
        var expected=json.readTree("""
            {"results":[{"identifier":42,"EM":101.007276,"name":"water","formula":"H2O","adduct":"M+H",
            "molecular_weight":100.0,"error_ppm":0,"ionizationScore":-2.0,"finalScore":-2.0,"cas":null,"inChiKey":null,
            "kegg_compound":"","kegg_uri":"","hmdb_compound":"HMDB1","hmdb_uri":"https://hmdb.ca/metabolites/HMDB1",
            "lipidmaps_compound":"","lipidmaps_uri":"","pubchem_compound":"123","pubchem_uri":"https://pubchem.ncbi.nlm.nih.gov/compound/123",
            "metlin_compound":"","metlin_uri":"","pathways":[{"name":"path","uri":"https://example.test/path"}]}]}
            """);
        assertEquals(expected,actual);
    }
    @ParameterizedTest @CsvSource({"'[M+H]+',POSITIVE,M+H","'[M-H]-',NEGATIVE,M-H","'[M+2H]2+',POSITIVE,M+2H","'[2M+Na]+',POSITIVE,2M+Na"})
    void adductRoundTripsPreserveChemistry(String canonical,IonizationMode mode,String legacy) {
        assertEquals(legacy,options.legacyAdduct(canonical,mode));
        var input=new LegacySearchOptions();input.setIonMode(mode.name().toLowerCase());input.setAdducts(List.of(legacy));
        assertEquals(Set.of(canonical),options.adducts(input));
    }
    @Test void allCatalogEntriesHaveUnambiguousLegacyNames() {
        for(var mode:List.of(IonizationMode.POSITIVE,IonizationMode.NEGATIVE)) {
            for(var d:ceu.biolab.cmm.shared.domain.adduct.AdductCatalog.definitionsFor(mode).values()) {
                var input=new LegacySearchOptions();input.setIonMode(mode.name().toLowerCase());input.setAdducts(List.of(d.legacyKey()));
                assertEquals(Set.of(d.canonical()),options.adducts(input),d.canonical());
            }
        }
    }
    @Test void advancedScoresScaleCurrentValuesWithoutRoundingOrRecomputation() {
        var f=feature(101.007276,2,"[M+H]+"); var score=new CompoundScores();score.setIonizationScore(.1);score.setAdductRelationScore(.5);
        score.setRtScore(Optional.of(.75));score.setFinalScore(Optional.of(.37));f.getAnnotationsByAdducts().getFirst().getAnnotations().getFirst().addScore(score);
        var row=json.valueToTree(mapper.annotated(List.of(f),IonizationMode.POSITIVE,true)).path("results").get(0);
        assertEquals(.2,row.path("ionizationScore").asDouble(),1e-9);assertEquals(1,row.path("adductRelationScore").asDouble());
        assertEquals(1.5,row.path("RTscore").asDouble());assertEquals(.74,row.path("finalScore").asDouble(),1e-9);assertEquals(2,row.path("RT").asDouble());
    }
    @Test void ppmUsesLegacyAbsoluteRoundingAndTheoreticalDenominator() {
        assertEquals(2,mapper.ppm(100.00016,100));assertEquals(2,mapper.ppm(99.99984,100));
        assertEquals(500000,mapper.ppm(150,100));
    }
    @Test void msmsResultUsesActualSourceAndOnlyLegacyFields() throws Exception {
        var a=new MSMSAnnotation();a.setCompound(compound());a.setSpectrumSource(SpectrumSource.PREDICTED);a.setMsmsCosineScore(.9);
        assertEquals(json.readTree("""
           {"results":[{"spectral_display_tools":"Predicted","identifier":42,"hmdb_compound":"HMDB1",
           "hmdb_uri":"https://hmdb.ca/metabolites/HMDB1","name":"water","formula":"H2O","mass":100.0,"score":0.9}]}
           """), json.valueToTree(mapper.msms(new MSMSSearchResponseDTO(List.of(a)))));
    }
    @Test void ceEnvelopeKeepsLegacyGroupsAndEmptyFragments() throws Exception {
        var input=new LegacyCeRequest();input.setMasses(List.of(101d));input.setEffectiveMobilities(List.of(10d));
        var response=new CemsSearchResponseDTO();var f=new CeFeatureAnnotationsDTO();f.setFeature(CeFeatureDTO.builder().mzValue(101).effectiveMobility(10).build());
        var group=new CeAnnotationsByAdductDTO("[M+H]+");group.addAnnotation(CeAnnotationDTO.builder().compound(compound()).massErrorPpm(-1.6).mobilityErrorPct(5d).build());
        f.addAnnotationsByAdduct(group);response.addFeature(f);
        var actual=json.valueToTree(new LegacyCeResponseMapper(mapper,options).map(response,input,"effmob"));
        assertEquals(101,actual.at("/compounds_found/0/input_mz").asDouble());assertEquals(10,actual.at("/compounds_found/0/input_effmob").asDouble());
        var a=actual.at("/compounds_found/0/annotations/0/compounds/0");
        assertEquals(42,a.path("id").asInt());assertFalse(a.has("identifier"));assertFalse(a.has("inChiKey"));assertTrue(a.has("cas_id"));
        assertEquals(10.5,a.path("effmob").asDouble());assertEquals(5,a.path("error_effmob").asInt());assertEquals(2,a.path("error_ppm").asInt());
        assertEquals("HMDB1",a.path("hmdb_id").asText());assertEquals(json.readTree("[]"),actual.path("fragments"));
    }
}
