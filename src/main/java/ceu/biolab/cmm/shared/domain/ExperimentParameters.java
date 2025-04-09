package ceu.biolab.cmm.shared.domain;

import java.util.Optional;

import lombok.Data;

@Data
public class ExperimentParameters {
    /**
     * The parameters of the user's experiment.
     */
    // TODO - class may be incomplete. Add parameters as needed.

    private Optional<IonMode> ionMode;
    private Optional<ModifierType> modifierType;

    public ExperimentParameters() {
        this.ionMode = Optional.empty();
        this.modifierType = Optional.empty();
    }

    // Empty constructor
    public static ExperimentParameters empty() {
        return new ExperimentParameters();
    }

    public String getIonModeStr() {
        return ionMode.map(IonMode::getValue).orElse("");
    }

    public String getModifierTypeStr() {
        return modifierType.map(ModifierType::getName).orElse("");
    }
}
