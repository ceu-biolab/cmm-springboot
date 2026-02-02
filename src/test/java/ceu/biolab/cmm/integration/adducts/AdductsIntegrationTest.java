package ceu.biolab.cmm.integration.adducts;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.StreamUtils;

@SpringBootTest
@AutoConfigureMockMvc
class AdductsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAdductsReturnsExpectedCatalog() throws Exception {
        String expectedResponse = loadJson("json/adducts/response.json");

        mockMvc.perform(get("/api/get/adducts"))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse, JsonCompareMode.STRICT));
    }

    private String loadJson(String path) throws IOException {
        Resource resource = new ClassPathResource(path);
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }
}
