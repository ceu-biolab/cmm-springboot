package ceu.biolab.cmm.adapters.simpleSearchAdapter.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ceu.biolab.cmm.shared.domain.MzToleranceMode;
import ceu.biolab.cmm.shared.domain.IonizationMode;

import ceu.biolab.cmm.adapters.simpleSearchAdapter.domain.LegacyMassesMode;
import ceu.biolab.cmm.adapters.simpleSearchAdapter.domain.LegacyDatabase;
import ceu.biolab.cmm.adapters.simpleSearchAdapter.domain.LegacyMetaboliteType;

/**
 * Data Transfer Object for Simple Search Adapter requests.
 * Contains a single compound to search for.
 * All fields have defaults.
 * 
 */

@Data
public class SimpleSearchAdapterRequestDTO {

    
    // === DEFAULT VALUES ===
    private static final double DEFAULT_MASS = 0.0;
    private static final double DEFAULT_TOLERANCE = 10.0;
    private static final MzToleranceMode DEFAULT_TOLERANCE_MODE = MzToleranceMode.PPM;
    private static final LegacyDatabase DEFAULT_DATABASE = LegacyDatabase.ALL_EXCEPT_MINE; 
    private static final LegacyMetaboliteType DEFAULT_METABOLITE_TYPE = LegacyMetaboliteType.ALL_EXCEPT_PEPTIDES; 
    private static final LegacyMassesMode DEFAULT_MASSES_MODE = LegacyMassesMode.MZ; //The service must translate
    private static final IonizationMode DEFAULT_ION_MODE = IonizationMode.POSITIVE; 

    
    // Default adduct canonicals by ionization mode
    private static final Set<String> DEFAULT_ADDUCTS_POSITIVE = Set.of(
            "[M+H]+", "[M+2H]2+", "[M+Na]+", "[M+K]+", "[M+NH4]+", "[M+H-H2O]+"
    );
    private static final Set<String> DEFAULT_ADDUCTS_NEGATIVE = Set.of(
            "[M-H]-", "[M+Cl]-", "[M+FA-H]-", "[M-H2O]-"
    );
    private static final Set<String> DEFAULT_ADDUCTS_NEUTRAL = Set.of(
            "[M]"   
    );




    // === FIELDS ===

    // Because 0.0 (default) violates the @Positive constraint, the field becomes implicitly required:
    // a request without this value will fail validation. The rest can be null as there are correctly defined defaults.
    @Positive(message = "Mass must be greater than 0")
    private double mass;
    
    @PositiveOrZero(message = "Tolerance must be zero or positive")
    private double tolerance;

    @JsonProperty("tolerance_mode")
    private MzToleranceMode toleranceMode;
    
    private Set<LegacyDatabase> databases;
    
    @JsonProperty("metabolites_type")
    private LegacyMetaboliteType metaboliteTypes;
    
    @JsonProperty("masses_mode")
    private LegacyMassesMode massesMode;
    
    @JsonProperty("ion_mode")
    private IonizationMode ionMode;
    
    private Set<String> adducts;


    

    // === CONSTRUCTORS ===

    /**
     * Default constructor - all fields initialized with defaults.
     * Used for JSON deserialization.
     */

    public SimpleSearchAdapterRequestDTO() {

        this.mass = DEFAULT_MASS;
        this.tolerance = DEFAULT_TOLERANCE;
        this.toleranceMode = DEFAULT_TOLERANCE_MODE;
        this.databases = new HashSet<>(List.of(DEFAULT_DATABASE));
        this.metaboliteTypes = DEFAULT_METABOLITE_TYPE;
        this.massesMode = DEFAULT_MASSES_MODE;
        this.ionMode = DEFAULT_ION_MODE;
        this.adducts =DEFAULT_ADDUCTS_POSITIVE;
    }

    /**
     * Full constructor with all parameters.
     * Validates and applies defaults for null values.
     * Adducts are resolved from AdductCatalog based on ionization mode.
     */

    public SimpleSearchAdapterRequestDTO(double mass, double tolerance, MzToleranceMode toleranceMode, Set<LegacyDatabase> databases, LegacyMetaboliteType metaboliteTypes,
            LegacyMassesMode massesMode, IonizationMode ionMode, Set<String> adductCanonicals) {
        
        this.mass = mass;
        this.tolerance = tolerance <= 100 ? tolerance : DEFAULT_TOLERANCE;
        this.toleranceMode = toleranceMode != null ? toleranceMode : DEFAULT_TOLERANCE_MODE;
        this.databases = databases != null ? databases : new HashSet<>(List.of(DEFAULT_DATABASE));
        this.metaboliteTypes = metaboliteTypes != null ? metaboliteTypes : DEFAULT_METABOLITE_TYPE;
        this.massesMode = massesMode != null ? massesMode : DEFAULT_MASSES_MODE;
        this.ionMode = ionMode != null ? ionMode : DEFAULT_ION_MODE;
        this.adducts = adductCanonicals != null ? adductCanonicals : getDefaultAdductsForMode(this.ionMode);
    }

    /**
     * Returns the default adduct canonicals for the given ionization mode.
     */
    private Set<String> getDefaultAdductsForMode(IonizationMode mode) {
        return switch (mode) {
            case POSITIVE -> DEFAULT_ADDUCTS_POSITIVE;
            case NEGATIVE -> DEFAULT_ADDUCTS_NEGATIVE;
            case NEUTRAL -> DEFAULT_ADDUCTS_NEUTRAL;
        };
    }
}
