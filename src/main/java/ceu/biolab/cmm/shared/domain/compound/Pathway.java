package ceu.biolab.cmm.shared.domain.compound;

import java.util.Objects;

public class Pathway {
    private Integer pathwayId;
    private String pathwayName;
    private String pathwayMap;

    public Pathway(Integer pathwayId, String pathwayName, String pathwayMap) {
        if (pathwayId == null) {
            this.pathwayId = -1;
        } else {
            this.pathwayId = pathwayId;
        }
        if (pathwayName == null) {
            this.pathwayName = "";
        } else {
            this.pathwayName = pathwayName;
        }
        if (pathwayMap == null) {
            this.pathwayMap = "";
        } else {
            this.pathwayMap = pathwayMap;
        }
    }

    public Pathway() {
        this.pathwayId = -1;
        this.pathwayName = "";
        this.pathwayMap = "";
    }

    public int getPathwayId() {
        return pathwayId;
    }

    public void setPathwayId(Integer pathwayId) {
        if (pathwayId == null) {
            this.pathwayId = -1;
        } else {
            this.pathwayId = pathwayId;
        }
    }

    public String getPathwayName() {
        return pathwayName;
    }

    public void setPathwayName(String pathwayName) {
        this.pathwayName = pathwayName;
    }

    public String getPathwayMap() {
        return pathwayMap;
    }

    public void setPathwayMap(String pathwayMap) {
        this.pathwayMap = pathwayMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pathway pathway = (Pathway) o;
        return Objects.equals(pathwayId, pathway.pathwayId) && Objects.equals(pathwayName, pathway.pathwayName) && Objects.equals(pathwayMap, pathway.pathwayMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pathwayId, pathwayName, pathwayMap);
    }

    @Override
    public String toString() {
        return "Pathway [pathwayId=" + pathwayId + ", pathwayName=" + pathwayName + ", pathwayMap=" + pathwayMap + "]";
    }
}
