package ceu.biolab.cmm.rtSearch.domain.msFeature;

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
