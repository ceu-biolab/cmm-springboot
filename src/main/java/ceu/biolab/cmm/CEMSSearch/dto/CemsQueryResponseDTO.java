package ceu.biolab.cmm.CEMSSearch.dto;

import lombok.Data;

@Data
public class CemsQueryResponseDTO {
    private long compoundId;
    private String casId;
    private String compoundName;
    private String formula;
    private Double mass;
    private Long chargeType;
    private Long chargeNumber;
    private String formulaType;
    private Integer compoundType;
    private Double logp;
    private Double rtPred;
    private String inchi;
    private String inchiKey;
    private String smiles;
    private String lipidType;
    private Integer numChains;
    private Integer numberCarbons;
    private Integer doubleBonds;
    private String biologicalActivity;
    private String meshNomenclature;
    private String iupacClassification;
    private Double experimentalMz;
    private Double experimentalEffMob;
    private Double mobility;
    private Long ceExpPropMetadataId;
    private Integer ceExpPropId;
    private String bufferCode;
    private Integer polarityId;
    private Integer ionizationModeId;
}
