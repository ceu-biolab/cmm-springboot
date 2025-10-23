package ceu.biolab.cmm.scoreAnnotations.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class EvaluatedLipid extends EvaluatedCompound {
    private Lipid lipid;

    public EvaluatedLipid(Lipid lipid, double featureMz, double featureRtValue, String adduct, LipidScores scores, boolean isSignificative) {
        super(lipid, featureMz, featureRtValue, adduct, scores, isSignificative);
        this.lipid = lipid;
    }

    public EvaluatedLipid(Lipid lipid, double featureMz, double featureRtValue, String adduct, LipidScores scores) {
        super(lipid, featureMz, featureRtValue, adduct, scores);
        this.lipid = lipid;
    }

    @Override
    public LipidScores getScores() {
        return (LipidScores) super.getScores();
    }

    public int getNumberCarbons() {
        return lipid.getNumberCarbons();
    }

    public int getNumberDoubleBonds() {
        return lipid.getNumberDoubleBonds();
    }

    public String getLipidType() {
        return lipid.getLipidType();
    }

    public String getCategory() {
        return extractCodeFromBracket(lipid.getCategory().orElse(""));
    }

    public String getMainClass() {
        return extractCodeFromBracket(lipid.getMainClass().orElse(""));
    }

    public String getSubClass() {
        // Sometimes the attribute subclass is formatted with full code and needs to be extracted
        return extractCodeFromBracket(lipid.getSubClass().orElse(""));
    }

    /**
     * If the provided text contains a code in square brackets at the end (or anywhere), extract and return it.
     * Otherwise return the original text (or empty string).
     *   E.g. subclass: "C5 isoprenoids (hemiterpenes) [PR0101]" --> "PR0101"
     */
    private String extractCodeFromBracket(String text) {
        if (text == null || text.isEmpty()) return "";
        int startIdx = text.indexOf('[');
        int endIdx = text.indexOf(']');
        if (startIdx >= 0 && endIdx > startIdx) {
            return text.substring(startIdx + 1, endIdx).trim();
        }
        return text;
    }
}
