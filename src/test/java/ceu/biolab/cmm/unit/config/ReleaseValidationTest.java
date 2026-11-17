package ceu.biolab.cmm.unit.config;

import ceu.biolab.cmm.config.GlobalExceptionHandler;
import ceu.biolab.cmm.scoreAnnotations.controller.ScoreAnnotationsController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReleaseValidationTest {
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new ScoreAnnotationsController())
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{}", "null", "{\"feature\":null}",
            "{\"feature\":{\"mzValue\":100}}",
            "{\"feature\":{\"mzValue\":\"banana\",\"rtValue\":5}}",
            "{\"feature\":{\"mzValue\":100,\"rtValue\":\"banana\"}}",
            "{\"feature\":{\"mzValue\":100,\"rtValue\":-1}}",
            "{\"feature\":{\"mzValue\":100,\"rtValue\":5},\"annotationsByAdducts\":null}",
            "{\"feature\":{\"mzValue\":100,\"rtValue\":5},\"annotationsByAdducts\":[null]}",
            "{\"feature\":{\"mzValue\":100,\"rtValue\":5},\"annotationsByAdducts\":[{\"adduct\":\"[M+H]+\",\"annotations\":null}]}",
            "{\"feature\":{\"mzValue\":100,\"rtValue\":5},\"annotationsByAdducts\":[{\"adduct\":\"[M+H]+\",\"annotations\":[null]}]}",
            "{\"feature\":{\"mzValue\":100,\"rtValue\":5},\"annotationsByAdducts\":[{\"adduct\":\"[M+H]+\",\"annotations\":[{\"compound\":null}]}]}"
    })
    void malformedScoringFeaturesReturn400(String feature) throws Exception {
        mvc.perform(post("/api/score-annotations").contentType(MediaType.APPLICATION_JSON)
                .content("{\"features\":[" + feature + "]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void frameworkErrorsKeepTheirHttpStatuses() throws Exception {
        mvc.perform(get("/api/score-annotations")).andExpect(status().isMethodNotAllowed());
        mvc.perform(post("/api/score-annotations").contentType(MediaType.TEXT_PLAIN).content("hello"))
                .andExpect(status().isUnsupportedMediaType());
    }
}
