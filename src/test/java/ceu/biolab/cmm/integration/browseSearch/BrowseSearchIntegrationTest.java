package ceu.biolab.cmm.integration.browseSearch;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


    @SpringBootTest
    @AutoConfigureMockMvc
    public class BrowseSearchIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        private String loadJson(String path) throws IOException {
            Resource resource = new ClassPathResource(path);
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        }

        // this is an example that should work
        @Test
        void testBrowseSearchWithCompleteExample() throws Exception {
            String requestJson = loadJson("json/browseSearch/request1.json");
            String expectedResponse = loadJson("json/browseSearch/response1.json");

            mockMvc.perform(post("/api/browseSearch")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                            .andExpect(status().isOk());
        }
        @Test
        void testBrowseSearchWithNullName() throws Exception {
            String requestJson = loadJson("json/browseSearch/request2.json");
            String expectedResponse = loadJson("json/browseSearch/response2.json");
            //TODO si el nombre esta vacio, coge todos los valores posibles que tmb tienen formula null, por lo que hay que diferenciar si tienen nombre null y formula null o se pueden filtrar esos tm
            mockMvc.perform(post("/api/browseSearch")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isOk())
                    .andExpect(content().json(expectedResponse, JsonCompareMode.LENIENT));
        }

        @Test
        void testBrowseSearchWithNullFromula() throws Exception {
            String requestJson = loadJson("json/browseSearch/request3.json");
            String expectedResponse = loadJson("json/browseSearch/response3.json");
          mockMvc.perform(post("/api/browseSearch")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isOk())
                    .andExpect(content().json(expectedResponse, JsonCompareMode.STRICT));//TODO Pq no me coge el mismo numero de valores esperados
        }

        @Test
        void testBrowseSearchWithNullDtabase() throws Exception {
            String requestJson = loadJson("json/browseSearch/request4.json");
            String expectedResponse = loadJson("json/browseSearch/response5.json");
            mockMvc.perform(post("/api/browseSearch")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

        }

        @Test
        void testBrowseSearchWithNullMetabolite() throws Exception {
            String requestJson = loadJson("json/browseSearch/request5.json");
            String expectedResponse = loadJson("json/browseSearch/response5.json");
            mockMvc.perform(post("/api/browseSearch")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    //.andExpect(status().isBadRequest());//TODO Pq no me coge el mismo numero de valores esperados
                    .andExpect(status().isOk());
        }


    }
