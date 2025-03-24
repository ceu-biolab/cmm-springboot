package ceu.biolab.cmm.ccsSearch.domain;

import java.util.List;
import java.util.ArrayList;

public class IMFeature {
    private double requestMzValue;
    private double requestCcsValue;
    private List<AnnotationsByAdduct> annotations;

    public IMFeature(double requestMzValue, double requestCcsValue, List<AnnotationsByAdduct> annotations) {
        this.requestMzValue = requestMzValue;
        this.requestCcsValue = requestCcsValue;
        this.annotations = annotations;
    }

    public IMFeature(double requestMzValue, double requestCcsValue) {
        this.requestMzValue = requestMzValue;
        this.requestCcsValue = requestCcsValue;
        this.annotations = new ArrayList<>();
    }

    public void addAnnotations(AnnotationsByAdduct annotationsByAdduct) {
        if (annotationsByAdduct != null) {
            this.annotations.add(annotationsByAdduct);
        }
    }

    public double getRequestMzValue() {
        return requestMzValue;
    }

    public void setRequestMzValue(double requestedMzValue) {
        this.requestMzValue = requestedMzValue;
    }

    public double getRequestCcsValue() {
        return requestCcsValue;
    }

    public void setRequestCcsValue(double requestedCcsValue) {
        this.requestCcsValue = requestedCcsValue;
    }

    public List<AnnotationsByAdduct> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<AnnotationsByAdduct> annotations) {
        if (annotations == null) {
            this.annotations = new ArrayList<>();
        } else {
            this.annotations = new ArrayList<>(annotations);
        }
    }

    @Override
    public String toString() {
        return "IMFeature [requestMzValue=" + requestMzValue + ", requestCcsValue=" + requestCcsValue + ", annotations=" + annotations + "]";
    }
}
