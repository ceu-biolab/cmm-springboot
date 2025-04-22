package ceu.biolab.cmm.scoreAnnotations.domain;

import java.util.Optional;

import ceu.biolab.cmm.shared.domain.compound.Compound;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class Lipid extends Compound {
    public String lipidType;
    public int numberChains;
    public int numberCarbons;
    public int numberDoubleBonds;
    // Classification implicitly contains the category, main class, subclass and class level 4 if present
    // e.g. "PR010405": "PR" is category, "PR01" main class, "PR0104" subclass, "PR010405" class level 4
    public String classificationCode;

    public Optional<String> getCategory() {
        if (classificationCode == null || classificationCode.length() < 2) {
            return Optional.empty();
        }
        return Optional.of(classificationCode.substring(0, 2));
    }

    public Optional<String> getMainClass() {
        if (classificationCode == null || classificationCode.length() < 4) {
            return Optional.empty();
        }
        return Optional.of(classificationCode.substring(0, 4));
    }

    public Optional<String> getSubClass() {
        if (classificationCode == null || classificationCode.length() < 6) {
            return Optional.empty();
        }
        return Optional.of(classificationCode.substring(0, 6));
    }

    public Optional<String> getClassLevel4() {
        if (classificationCode == null || classificationCode.length() < 8) {
            return Optional.empty();
        }
        return Optional.of(classificationCode.substring(0, 8));
    }
}
