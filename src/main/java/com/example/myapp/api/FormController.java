package com.example.myapp.api;

import com.example.myapp.model.FormData;
import com.example.myapp.repository.FormDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class FormController {
    private static final Logger logger = LoggerFactory.getLogger(FormController.class);

    @Autowired
    private FormDataRepository formDataRepository;

    @PostMapping("/submit-form")
    public FormData handleFormSubmission(@RequestBody FormData formData) {
        logger.info("✅ Received JSON Data: {}", formData);
        return formDataRepository.save(formData); // Save to PostgreSQL
    }

    @GetMapping("/submissions")
    public List<FormData> getAllSubmissions() {
        return formDataRepository.findAll(); // Retrieve all submissions
    }
}
