package ceu.biolab.cmm.CEMSSearch.domain;

import java.util.Locale;
import java.util.Map;
import java.util.OptionalInt;

public final class CeBufferDictionary {

    private static final Map<String, Integer> BUFFER_BY_NAME = Map.of(
            normalize("formic acid 1M"), 1,
            normalize("acetic acid 10% v:v"), 2,
            normalize("formic acid 0.1M"), 3
    );

    private CeBufferDictionary() {
    }

    public static OptionalInt findBufferId(String backgroundElectrolyte) {
        if (backgroundElectrolyte == null) {
            return OptionalInt.empty();
        }
        Integer bufferId = BUFFER_BY_NAME.get(normalize(backgroundElectrolyte));
        return bufferId == null ? OptionalInt.empty() : OptionalInt.of(bufferId);
    }

    private static String normalize(String value) {
        return value.toLowerCase(Locale.ROOT).trim();
    }

    // TODO: add mappings for the remaining CE buffers once they are available.
}
