package ceu.biolab.cmm.rtSearch.model.compound;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;


public class Compound {
    private final Integer compound_id;
    private final String cas_id;
    private final String compound_name;
    private final String formula;
    private final Double mass;
    private final Integer charge_type;
    private final Integer charge_number;
    private final Integer formula_type;
    private final Integer compound_type;
    private final Integer compound_status;
    private final Integer formula_type_int;
    private final Double logP;
    private final Double rt_pred;


    public Compound(Integer compound_id, String cas_id, String compound_name, String formula, Double mass,
                    Integer charge_type, Integer charge_number, Integer formula_type, Integer compound_type,
                    Integer compound_status, Integer formula_type_int, Double logP, Double rt_pred) {
        this.compound_id = compound_id;
        this.cas_id = cas_id;
        this.compound_name = compound_name;
        this.formula = formula;
        this.mass = mass;
        this.charge_type = charge_type;
        this.charge_number = charge_number;
        this.formula_type = formula_type;
        this.compound_type = compound_type;
        this.compound_status = compound_status;
        this.formula_type_int = formula_type_int;
        this.logP = logP;
        this.rt_pred = rt_pred;
    }


    public Integer getCompound_id() {
        return compound_id;
    }

    public String getCas_id() {
        return cas_id;
    }

    public String getCompound_name() {
        return compound_name;
    }

    public String getFormula() {
        return formula;
    }

    public Double getMass() {
        return mass;
    }

    public Integer getCharge_type() {
        return charge_type;
    }

    public Integer getCharge_number() {
        return charge_number;
    }

    public Integer getFormula_type() {
        return formula_type;
    }

    public Integer getCompound_type() {
        return compound_type;
    }

    public Integer getCompound_status() {
        return compound_status;
    }

    public Integer getFormula_type_int() {
        return formula_type_int;
    }

    public Double getLogP() {
        return logP;
    }

    public Double getRt_pred() {
        return rt_pred;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + Objects.hashCode(this.compound_id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Compound other = (Compound) obj;
        return Objects.equals(this.compound_id, other.compound_id);
    }

    @Override
    public String toString() {
        return "Compound{" +
                "compound_id=" + this.compound_id + ", cas_id='" + this.cas_id + '\'' +
                ", compound_name='" + this.compound_name + '\'' +
                ", formula='" + this.formula + '\'' + ", mass=" + this.mass + ", charge_type=" + this.charge_type +
                ", charge_number=" + this.charge_number + ", formula_type=" + this.formula_type +
                ", compound_type=" + this.compound_type + ", compound_status=" + this.compound_status +
                ", formula_type_int=" + this.formula_type_int + ", logP=" + this.logP + ", rt_pred=" + this.rt_pred + '}';
    }

}
