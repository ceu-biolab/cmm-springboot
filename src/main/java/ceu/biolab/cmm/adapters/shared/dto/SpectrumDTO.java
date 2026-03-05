package ceu.biolab.cmm.adapters.shared.dto;

import lombok.Data;

/**
 * DTO representing a single spectrum peak (mz / intensity).
 *
 * Used in composite_spectra-style fields across adapters.
 */
@Data
public class SpectrumDTO {

    private double mz;
    private double intensity;

    public SpectrumDTO() {
        // default constructor for JSON deserialization
    }

    public SpectrumDTO(double mz, double intensity) {
        this.mz = mz;
        this.intensity = intensity;
    }
}
