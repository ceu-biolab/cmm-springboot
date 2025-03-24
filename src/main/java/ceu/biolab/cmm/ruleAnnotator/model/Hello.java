package ceu.biolab.cmm.ruleAnnotator.model;

public class Hello {
    private String val;

    public Hello(String val) {
        super();
        this.val = val;
    }

    public String getVal() {
        return val;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Hello [");
        if (val != null)
            builder.append("val=").append(val);
        builder.append("]");
        return builder.toString();
    }
}
