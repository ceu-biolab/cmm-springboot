package ceu.biolab.cmm.unit.scoreAnnotations.domain;

import org.junit.jupiter.api.Test;

import ceu.biolab.cmm.scoreAnnotations.domain.Lipid;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Optional;

public class LipidTest {
    
    @Test
    public void testGetCategory() {
        // Valid case
        Lipid lipid = Lipid.builder().classificationCode("PR010405").build();
        assertEquals(Optional.of("PR"), lipid.getCategory());
        
        // Exact length classification code
        lipid = Lipid.builder().classificationCode("PR").build();
        assertEquals(Optional.of("PR"), lipid.getCategory());
        
        // Short classification code
        lipid = Lipid.builder().classificationCode("P").build();
        assertEquals(Optional.empty(), lipid.getCategory());
        
        // Empty classification code
        lipid = Lipid.builder().classificationCode("").build();
        assertEquals(Optional.empty(), lipid.getCategory());
        
        // Null classification code
        lipid = Lipid.builder().classificationCode(null).build();
        assertEquals(Optional.empty(), lipid.getCategory());
    }
    
    @Test
    public void testGetMainClass() {
        // Valid case
        Lipid lipid = Lipid.builder().classificationCode("PR010405").build();
        assertEquals(Optional.of("PR01"), lipid.getMainClass());
        
        // Exact length classification code
        lipid = Lipid.builder().classificationCode("PR01").build();
        assertEquals(Optional.of("PR01"), lipid.getMainClass());
        
        // Short classification code
        lipid = Lipid.builder().classificationCode("PR0").build();
        assertEquals(Optional.empty(), lipid.getMainClass());
        
        // Empty classification code
        lipid = Lipid.builder().classificationCode("").build();
        assertEquals(Optional.empty(), lipid.getMainClass());
        
        // Null classification code
        lipid = Lipid.builder().classificationCode(null).build();
        assertEquals(Optional.empty(), lipid.getMainClass());
    }
    
    @Test
    public void testGetSubClass() {
        // Valid case
        Lipid lipid = Lipid.builder().classificationCode("PR010405").build();
        assertEquals(Optional.of("PR0104"), lipid.getSubClass());
        
        // Exact length classification code
        lipid = Lipid.builder().classificationCode("PR0104").build();
        assertEquals(Optional.of("PR0104"), lipid.getSubClass());
        
        // Short classification code
        lipid = Lipid.builder().classificationCode("PR010").build();
        assertEquals(Optional.empty(), lipid.getSubClass());
        
        // Empty classification code
        lipid = Lipid.builder().classificationCode("").build();
        assertEquals(Optional.empty(), lipid.getSubClass());
        
        // Null classification code
        lipid = Lipid.builder().classificationCode(null).build();
        assertEquals(Optional.empty(), lipid.getSubClass());
    }
    
    @Test
    public void testGetClassLevel4() {
        // Valid case
        Lipid lipid = Lipid.builder().classificationCode("PR010405").build();
        assertEquals(Optional.of("PR010405"), lipid.getClassLevel4());
        
        // Longer classification code
        lipid = Lipid.builder().classificationCode("PR01040599").build();
        assertEquals(Optional.of("PR010405"), lipid.getClassLevel4());
        
        // Exact length classification code
        lipid = Lipid.builder().classificationCode("PR010405").build();
        assertEquals(Optional.of("PR010405"), lipid.getClassLevel4());
        
        // Short classification code
        lipid = Lipid.builder().classificationCode("PR01040").build();
        assertEquals(Optional.empty(), lipid.getClassLevel4());
        
        // Empty classification code
        lipid = Lipid.builder().classificationCode("").build();
        assertEquals(Optional.empty(), lipid.getClassLevel4());
        
        // Null classification code
        lipid = Lipid.builder().classificationCode(null).build();
        assertEquals(Optional.empty(), lipid.getClassLevel4());
    }
}
