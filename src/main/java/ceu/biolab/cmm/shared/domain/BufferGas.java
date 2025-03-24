package ceu.biolab.cmm.shared.domain;

public enum BufferGas {
    N2("N2"), 
    HE("He");

    private final String bufferGas;

    BufferGas(String bufferGas) {
        this.bufferGas = bufferGas;
    }

    public String getBufferGas() {
        return bufferGas;
    }

    public static BufferGas fromString(String text) {
        for (BufferGas b : BufferGas.values()) {
            if (b.bufferGas.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return bufferGas;
    }
}
