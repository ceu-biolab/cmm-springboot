package ceu.biolab.cmm.ccsSearch.dto;

import lombok.Data;

@Data
public class CcsFeatureQueryDTO {
    private double ccsLower;
    private double ccsUpper;
    private double massLower;
    private double massUpper;
    private String bufferGas;
    private String adduct;
    // private BufferGas buffer_gas

    public CcsFeatureQueryDTO(double ccsLower, double ccsUpper, double massLower, double massUpper, String bufferGas, String adduct) {
        this.ccsLower = ccsLower;
        this.ccsUpper = ccsUpper;
        this.massLower = massLower;
        this.massUpper = massUpper;
        this.bufferGas = bufferGas;
        this.adduct = adduct;
    }
}
