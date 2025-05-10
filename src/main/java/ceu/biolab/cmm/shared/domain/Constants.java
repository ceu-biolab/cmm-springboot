package ceu.biolab.cmm.shared.domain;

public final class Constants {
    // Example constant
    public static final double PPM_FACTOR = 0.000001;

    private Constants() {
        throw new AssertionError("Cannot instantiate Constants class");
    }
    public static final double PROTON_WEIGTH = 1.0073d;
    public static final double BIGGEST_ISOTOPE = 2.01; // Chlorine is the biggest gap for an isotope detected
    public static double ADDUCT_AUTOMATIC_DETECTION_WINDOW = 0.05D;
    public static final double ELECTRON_MONOISOTOPIC_MASS = 0.0005485794321631d;


}
