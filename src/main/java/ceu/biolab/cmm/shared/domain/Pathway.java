package ceu.biolab.cmm.shared.domain;

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
    public String toString() {
        return "Pathway [pathwayId=" + pathwayId + ", pathwayName=" + pathwayName + ", pathwayMap=" + pathwayMap + "]";
    }
}
