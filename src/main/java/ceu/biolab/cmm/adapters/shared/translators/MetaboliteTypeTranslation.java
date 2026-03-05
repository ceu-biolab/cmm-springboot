package ceu.biolab.cmm.adapters.shared.translators;

import ceu.biolab.cmm.adapters.shared.domain.LegacyMetaboliteType;
import ceu.biolab.cmm.shared.domain.MetaboliteType;

public final class MetaboliteTypeTranslation {

    private MetaboliteTypeTranslation() {
    }

    public static MetaboliteType toMetaboliteType(LegacyMetaboliteType legacyMetaboliteType) {
        if (legacyMetaboliteType == LegacyMetaboliteType.ONLY_LIPIDS) {
            return MetaboliteType.ONLYLIPIDS;
        } else {
            return MetaboliteType.ALL;
        }
    }
}
