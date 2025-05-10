package ceu.biolab.cmm.integration.browseSearch;

import ceu.biolab.cmm.browseSearch.dto.BrowseSearchRequest;
import ceu.biolab.cmm.shared.domain.Database;
import ceu.biolab.cmm.shared.domain.MetaboliteType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.antlr.runtime.BitSet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


    @SpringBootTest
    @AutoConfigureMockMvc
    public class BrowseSearchIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper; // para convertir objetos a JSON

        @Test
        public void testBrowseSearchReturnsOk() throws Exception {
            // 1. Crear un request simulado
            BrowseSearchRequest request = new BrowseSearchRequest();
            request.setCompound_name("glucose");
            request.setFormula("C6H12O6");
            List<Database> list=new ArrayList<>();
            list.add(Database.ALL);
            request.setDatabases(list);
            request.setMetaboliteType(MetaboliteType.ALL);
            request.setExact_name(true);
          //TODO comprobar como puedo hacer un constructor para cuando metabolite y database sea null

            // 2. Convertir el objeto a JSON
            String jsonRequest = objectMapper.writeValueAsString(request);

            // 3. Hacer la petición POST
            mockMvc.perform(MockMvcRequestBuilders.post("/api/browse")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isOk()) // 200 OK
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }
        // cuantos test
    }
