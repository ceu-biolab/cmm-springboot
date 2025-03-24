package ceu.biolab.cmm.rtSearch.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import ceu.biolab.cmm.rtSearch.model.FormData;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class FormController {
    private static final Logger logger = LoggerFactory.getLogger(FormController.class);

    // Temporary in-memory list to store submissions
    private final List<FormData> submissions = new ArrayList<>();


    @PostMapping("/submit-form")
    public FormData handleFormSubmission(@RequestBody FormData formData) {
        logger.info("✅ Received JSON Data: {}", formData);

        // Store in memory instead of a database
        submissions.add(formData);

        return formData; // Returning the received data
    }

    @GetMapping("/submissions")
    public List<FormData> getAllSubmissions() {
        return submissions; // Retrieve all stored submissions
    }
}