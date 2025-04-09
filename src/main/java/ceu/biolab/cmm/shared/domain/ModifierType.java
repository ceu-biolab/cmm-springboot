package ceu.biolab.cmm.shared.domain;

public enum ModifierType {
    NONE("none"),
    NH3("NH3"),
    HCOO("HCOO"),
    CH3COO("CH3COO"),
    HCOONH3("HCOONH3"),
    CH3COONH3("CH3COONH3")
    ;
    
    private final String name;

    ModifierType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static ModifierType fromName(String name) {
        for (ModifierType type : ModifierType.values()) {
            if (type.getName().equals(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown modifier type: " + name);
    }
}
