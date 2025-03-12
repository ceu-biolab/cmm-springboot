package com.example.myapp.model.compound;

import java.util.List;

public class CMMCompound extends Compound{

    public CMMCompound(Integer compound_id, String cas_id, String compound_name, String formula, Double mass, Integer charge_type, Integer charge_number, Integer formula_type, Integer compound_type, Integer compound_status, Integer formula_type_int, Double logP, Double rt_pred) {
        super(compound_id, cas_id, compound_name, formula, mass, charge_type, charge_number, formula_type, compound_type, compound_status, formula_type_int, logP, rt_pred);
    }
}
