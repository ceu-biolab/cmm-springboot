package ceu.biolab.cmm.ccsSearch.dto;

public class CcsFeatureQuery {
    private double ccsLower;
    private double ccsUpper;
    private double massLower;
    private double massUpper;
    private String bufferGas;
    private String adduct;
    // private BufferGas buffer_gas

    public CcsFeatureQuery(double ccsLower, double ccsUpper, double massLower, double massUpper, String bufferGas, String adduct) {
        this.ccsLower = ccsLower;
        this.ccsUpper = ccsUpper;
        this.massLower = massLower;
        this.massUpper = massUpper;
        this.bufferGas = bufferGas;
        this.adduct = adduct;
    }

    public double getCcsLower() {
        return ccsLower;
    }

    public void setCcsLower(double ccsLower) {
        this.ccsLower = ccsLower;
    }

    public double getCcsUpper() {
        return ccsUpper;
    }

    public void setCcsUpper(double ccsUpper) {
        this.ccsUpper = ccsUpper;
    }

    public double getMassLower() {
        return massLower;
    }

    public void setMassLower(double massLower) {
        this.massLower = massLower;
    }

    public double getMassUpper() {
        return massUpper;
    }

    public void setMassUpper(double massUpper) {
        this.massUpper = massUpper;
    }

    public String getBufferGas() {
        return bufferGas;
    }

    public void setBufferGas(String bufferGas) {
        this.bufferGas = bufferGas;
    }

    public String getAdduct() {
        return adduct;
    }

    public void setAdduct(String adduct) {
        this.adduct = adduct;
    }
}
