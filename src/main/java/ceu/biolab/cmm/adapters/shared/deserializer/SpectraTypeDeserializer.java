package ceu.biolab.cmm.adapters.shared.deserializer;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import ceu.biolab.cmm.adapters.shared.domain.SpectraType;

/**
 * Jackson deserializer for adapter-layer SpectraType, accepting
 * legacy strings like "experimental" and "predicted".
 */
public class SpectraTypeDeserializer extends JsonDeserializer<SpectraType> {

	@Override
	public SpectraType deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		String value = p.getText();
		if (value == null) {
			return SpectraType.EXPERIMENTAL;
		}
		value = value.trim();
		if (value.isEmpty()) {
			return SpectraType.EXPERIMENTAL;
		}
		try {
			return SpectraType.fromString(value);
		} catch (IllegalArgumentException e) {
			throw new IOException("Invalid SpectraType: " + value, e);
		}
	}
}
