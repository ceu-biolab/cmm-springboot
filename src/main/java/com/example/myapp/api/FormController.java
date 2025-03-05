package com.example.myapp.api;

import com.example.myapp.model.FormData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000") // Allow frontend requests
public class FormController {
    private static final Logger logger = LoggerFactory.getLogger(FormController.class);

    // Temporary in-memory list to store submissions
    private final List<FormData> submissions = new ArrayList<>();

    @GetMapping("/ping")
    public String ping() {
        return "Backend is working!";
    }

    /*@PostMapping("/submit-form")
    public FormData handleFormSubmission(@RequestBody FormData formData) {
        logger.info("✅ Received JSON Data: {}", formData);

        // Store in memory instead of a database
        //submissions.add(formData);

        return formData; // Returning the received data
    }

     */

    /*@GetMapping("/submissions")
    public List<FormData> getAllSubmissions() {
        return submissions; // Retrieve all stored submissions
    }

     */



}
