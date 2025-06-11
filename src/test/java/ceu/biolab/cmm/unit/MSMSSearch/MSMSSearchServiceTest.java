package ceu.biolab.cmm.unit.MSMSSearch;

import ceu.biolab.cmm.MSMS.domain.CIDEnergy;

import ceu.biolab.cmm.MSMS.domain.MSMSAnotation;
import ceu.biolab.cmm.MSMS.domain.Peak;
import ceu.biolab.cmm.MSMS.domain.ToleranceMode;
import ceu.biolab.cmm.MSMS.dto.MSMSSearchRequestDTO;
import ceu.biolab.cmm.MSMS.dto.MSMSSearchResponseDTO;
import ceu.biolab.cmm.MSMS.repository.MSMSSearchRepository;
import ceu.biolab.cmm.MSMS.service.MSMSSearchService;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
public class MSMSSearchServiceTest {
    private MSMSSearchRepository msmsSearchRepository;
    private MSMSSearchService msmsSearchService;
    @Autowired
    private DataSource dataSource;

    @BeforeEach
    public void setup() {

        NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        ResourceLoader resourceLoader = new DefaultResourceLoader();

        // Carga el schema y datos iniciales si tienes .sql
        msmsSearchRepository= new MSMSSearchRepository(jdbcTemplate, resourceLoader);
        msmsSearchService = new MSMSSearchService(msmsSearchRepository);
    }

    @Test
    public void testFindMatchingCompoundsAndSpectra_returnsValidResponse() throws Exception {
        MSMSSearchRequestDTO request = new MSMSSearchRequestDTO();
        request.setIonizationMode(IonizationMode.POSITIVE);
        request.setTolerancePrecursorIon(0.1);
        request.setToleranceModePrecursorIon(ToleranceMode.mDA);
        request.setToleranceFragments(0.5);
        request.setToleranceModeFragments(ToleranceMode.mDA);
        request.setPrecursorIonMZ(147.0);
        request.setAdducts(List.of("M+H"));
        Set<Peak> spectrum = new HashSet<>();
         spectrum.add(new Peak(40.948 ,0.174));
         spectrum.add(new Peak(56.022 ,0.424));
         spectrum.add(new Peak(84.37 ,53.488));
         spectrum.add(new Peak(101.50, 8.285));
         spectrum.add(new Peak(102.401, 0.775));
        spectrum.add(new Peak(129.670, 100.000));
        spectrum.add(new Peak(146.966, 20.070));
        request.getSpectrum().addAll(spectrum);
        request.setCIDEnergy(CIDEnergy.LOW);

        MSMSSearchResponseDTO result = msmsSearchRepository.findMatchingCompoundsAndSpectra(request);

        assertNotNull(result);
        assertNotNull(result.getMsmsList());
       for(MSMSAnotation anotation : result.getMsmsList()){
           System.out.println("Compound id: " + anotation.getCompoundId()+" compound name: " + anotation.getCompoundName()+" compound formula: "+anotation.getFormula()
                   +" Precursor ion mass: "+anotation.getPrecursorMz()+" compound score: "+anotation.getScore());
       }
    }
}

