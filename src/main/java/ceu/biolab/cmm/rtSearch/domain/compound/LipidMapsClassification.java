package ceu.biolab.cmm.rtSearch.domain.compound;

public class LipidMapsClassification {
    protected String category;
    protected String mainClass;
    protected String subClass;
    protected String classLevel4;

    public LipidMapsClassification(String category, String mainClass, String subClass, String classLevel4) {
        this.category = category;
        this.mainClass = mainClass;
        this.subClass = subClass;
        this.classLevel4 = classLevel4;
    }

    public String getCategory() {
        return this.category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getMainClass() {
        return this.mainClass;
    }

    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    public String getSubClass() {
        return this.subClass;
    }

    public void setSubClass(String subClass) {
        this.subClass = subClass;
    }

    public String getClassLevel4() {
        return this.classLevel4;
    }

    public void setClassLevel4(String classLevel4) {
        this.classLevel4 = classLevel4;
    }

    @Override
    public String toString() {
        return "\n\tCategory: " + this.category + "\n\tMain Class: " + this.mainClass + "\n\tSubclass: " + this.subClass
                + "\n\tClass level 4: " + this.classLevel4;
    }
}
