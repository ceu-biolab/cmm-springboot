package ceu.biolab.cmm.shared.domain.msFeature;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import com.fasterxml.jackson.databind.JsonMappingException;
import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import ceu.biolab.cmm.shared.domain.compound.Compound;
import lombok.Data;

@Data
public class AnnotatedFeature {
    @JsonDeserialize(using = IMSFeatureDeserializer.class)
    @NotNull
    private IMSFeature feature;
    @NotNull
    private List<@NotNull @Valid AnnotationsByAdduct> annotationsByAdducts;

    public AnnotatedFeature() {
        this.annotationsByAdducts = new ArrayList<>();
    }

    public AnnotatedFeature(double mzValue) {
        this.feature = new MSFeature(mzValue);
        this.annotationsByAdducts = new ArrayList<>();
    }

    public AnnotatedFeature(IMSFeature feature) {
        this.feature = feature;
        this.annotationsByAdducts = new ArrayList<>();
    }

    public Optional<AnnotationsByAdduct> findAnnotationByAdduct(String adduct) {
        for (AnnotationsByAdduct annotationsByAdduct : annotationsByAdducts) {
            if (annotationsByAdduct.getAdduct().equals(adduct)) {
                return Optional.of(annotationsByAdduct);
            }
        }
        return Optional.empty();
    }

    public void addAnnotationByAdduct(AnnotationsByAdduct annotationsByAdduct) {
        this.annotationsByAdducts.add(annotationsByAdduct);
    }

    public void addCompoundForAdduct(String adduct, Compound compound) {
        Optional<AnnotationsByAdduct> annotationsByAdduct = findAnnotationByAdduct(adduct);
        if (annotationsByAdduct.isPresent()) {
            annotationsByAdduct.get().addUnannotatedCompound(compound);
        } else {
            AnnotationsByAdduct newAnnotationsByAdduct = new AnnotationsByAdduct(adduct);
            newAnnotationsByAdduct.addUnannotatedCompound(compound);
            this.annotationsByAdducts.add(newAnnotationsByAdduct);
        }
    }

    static class IMSFeatureDeserializer extends JsonDeserializer<IMSFeature> {

        @Override
        public IMSFeature deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
            JsonNode node = parser.getCodec().readTree(parser);
            if (node == null || node.isNull()) {
                return null;
            }

            if (!node.isObject()) {
                throw JsonMappingException.from(parser, "feature must be an object");
            }
            double mzValue = number(parser, node.get("mzValue"), "mzValue", true);

            Optional<Double> intensity = Optional.empty();
            JsonNode intensityNode = node.get("intensity");
            if (intensityNode != null && !intensityNode.isNull()) {
                intensity = Optional.of(number(parser, intensityNode, "intensity", false));
            }

            JsonNode rtNode = node.hasNonNull("rtValue") ? node.get("rtValue") : node.get("retentionTime");
            if (rtNode != null && !rtNode.isNull()) {
                double rtValue = number(parser, rtNode, "rtValue", false);
                if (intensity.isPresent()) {
                    return new LCMSFeature(rtValue, mzValue, intensity.get());
                }
                return new LCMSFeature(rtValue, mzValue);
            }

            if (intensity.isPresent()) {
                return new MSFeature(mzValue, intensity.get());
            }
            return new MSFeature(mzValue);
        }

        private double number(JsonParser parser, JsonNode value, String name, boolean positive) throws IOException {
            if (value == null || !value.isNumber() || !Double.isFinite(value.asDouble())
                    || (positive ? value.asDouble() <= 0 : value.asDouble() < 0)) {
                throw JsonMappingException.from(parser,
                        name + " must be a finite " + (positive ? "positive" : "non-negative") + " number");
            }
            return value.asDouble();
        }
    }
}
