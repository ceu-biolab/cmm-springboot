package ceu.biolab.cmm.CEMSMarkers.domain;

import ceu.biolab.cmm.CEMSSearch.domain.CePolarity;

public record MarkerMobility(double effectiveMobility,
                             String bufferCode,
                             CePolarity polarity) {
}
