package ceu.biolab.cmm.CEMSSearch.domain;

import ceu.biolab.cmm.CEMSSearch.dto.CemsQueryResponseDTO;
import ceu.biolab.cmm.shared.domain.FormulaType;
import ceu.biolab.cmm.shared.domain.compound.CMMCompound;
import ceu.biolab.cmm.shared.domain.compound.CompoundType;
import java.util.HashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CemsCompoundMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(CemsCompoundMapper.class);

    private CemsCompoundMapper() {
    }

    public static CMMCompound toCompound(CemsQueryResponseDTO candidate) {
        if (candidate == null) {
            throw new IllegalArgumentException("Candidate compound cannot be null");
        }
        long candidateId = candidate.getCompoundId();
        int compoundId = safeLongToInt(candidateId, "compoundId", candidateId);

        Double massValue = candidate.getMass();
        if (massValue == null) {
            LOGGER.warn("Missing mass for compound {}, defaulting to 0", candidateId);
            massValue = 0d;
        }

        int chargeType = safeLongToInt(candidate.getChargeType(), "chargeType", candidateId);
        int chargeNumber = safeLongToInt(candidate.getChargeNumber(), "chargeNumber", candidateId);

        FormulaType formulaType = resolveFormulaType(candidate, candidateId);
        CompoundType compoundType = resolveCompoundType(candidate.getCompoundType(), candidateId);

        return CMMCompound.builder()
                .compoundId(compoundId)
                .casId(candidate.getCasId())
                .compoundName(candidate.getCompoundName())
                .formula(candidate.getFormula())
                .mass(massValue)
                .chargeType(chargeType)
                .chargeNumber(chargeNumber)
                .formulaType(formulaType)
                .compoundType(compoundType)
                .logP(candidate.getLogp())
                .rtPred(candidate.getRtPred())
                .inchi(candidate.getInchi())
                .inchiKey(candidate.getInchiKey())
                .smiles(candidate.getSmiles())
                .lipidType(candidate.getLipidType())
                .numChains(candidate.getNumChains())
                .numCarbons(candidate.getNumberCarbons())
                .doubleBonds(candidate.getDoubleBonds())
                .biologicalActivity(candidate.getBiologicalActivity())
                .meshNomenclature(candidate.getMeshNomenclature())
                .iupacClassification(candidate.getIupacClassification())
                .mol2(null)
                .pathways(new HashSet<>())
                .keggID(candidate.getKeggId())
                .lmID(candidate.getLmId())
                .hmdbID(candidate.getHmdbId())
                .agilentID(candidate.getAgilentId())
                .pcID(candidate.getPcId())
                .chebiID(candidate.getChebiId())
                .inHouseID(candidate.getInHouseId())
                .aspergillusID(candidate.getAspergillusId())
                .knapsackID(candidate.getKnapsackId())
                .npatlasID(candidate.getNpatlasId())
                .fahfaID(candidate.getFahfaId())
                .ohPositionID(candidate.getOhPosition())
                .aspergillusWebName(candidate.getAspergillusWebName())
                .build();
    }

    private static FormulaType resolveFormulaType(CemsQueryResponseDTO candidate, long candidateId) {
        String formulaTypeValue = candidate.getFormulaType();
        if (formulaTypeValue != null) {
            try {
                return FormulaType.valueOf(formulaTypeValue.toUpperCase());
            } catch (IllegalArgumentException ex) {
                LOGGER.warn("Unknown formula type '{}' for compound {}", formulaTypeValue, candidateId);
            }
        }
        Integer formulaTypeInt = candidate.getFormulaTypeInt();
        if (formulaTypeInt != null) {
            try {
                return FormulaType.getFormulaTypefromInt(formulaTypeInt);
            } catch (IllegalArgumentException ex) {
                LOGGER.warn("Unknown formula type int {} for compound {}", formulaTypeInt, candidateId);
            }
        }
        return FormulaType.inferFromFormula(candidate.getFormula()).orElse(null);
    }

    private static CompoundType resolveCompoundType(Integer compoundTypeRaw, long candidateId) {
        CompoundType compoundType = null;
        if (compoundTypeRaw != null) {
            try {
                compoundType = CompoundType.fromDbValue(compoundTypeRaw);
            } catch (IllegalArgumentException ex) {
                LOGGER.warn("Unknown compound type {} for compound {}", compoundTypeRaw, candidateId);
            }
        }
        return compoundType == null ? CompoundType.NON_LIPID : compoundType;
    }

    private static int safeLongToInt(Long value, String field, long candidateId) {
        if (value == null) {
            LOGGER.warn("Missing {} for compound {}, defaulting to 0", field, candidateId);
            return 0;
        }
        try {
            return Math.toIntExact(value);
        } catch (ArithmeticException ex) {
            LOGGER.warn("{} {} exceeds integer range, truncating", field, value);
            return value > 0 ? Integer.MAX_VALUE : Integer.MIN_VALUE;
        }
    }

    private static int safeLongToInt(long value, String field, long candidateId) {
        try {
            return Math.toIntExact(value);
        } catch (ArithmeticException ex) {
            LOGGER.warn("{} {} exceeds integer range, truncating for response", field, value);
            return value > 0 ? Integer.MAX_VALUE : Integer.MIN_VALUE;
        }
    }
}
