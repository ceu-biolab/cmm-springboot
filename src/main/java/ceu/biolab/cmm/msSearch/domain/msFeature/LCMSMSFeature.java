package ceu.biolab.cmm.msSearch.domain.msFeature;

import ceu.biolab.cmm.shared.domain.msFeature.MSPeak;

import java.util.List;

public class LCMSMSFeature implements LCMSFeature, MSMSFeature{

    @Override
    public double getRT() {
        return 0;
    }

    @Override
    public List<MSPeak> getProductIons() {
        return null;
    }
}
