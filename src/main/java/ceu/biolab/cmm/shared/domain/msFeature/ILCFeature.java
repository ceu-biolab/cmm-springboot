package ceu.biolab.cmm.shared.domain.msFeature;

public interface ILCFeature extends IMSFeature {
    double getRtValue();

    void setRtValue(double rtValue);

    default double getRetentionTime() {
        return getRtValue();
    }

    default void setRetentionTime(double rtValue) {
        setRtValue(rtValue);
    }
}
