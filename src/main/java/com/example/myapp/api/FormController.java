package com.example.myapp.api;

import com.example.myapp.FormData;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/*@RestController
@RequestMapping("/api")  // we need to prefix all JSON endpoints with "/api"
public class HelloRestController {

    @GetMapping("/hello")
    public Map<String, String> sayHello() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Hello from Spring Boot JSON API!");
        return response;  // This will be returned as JSON
    }

 */

@RestController
@RequestMapping("/api")
public class FormController {

    private FormData storedData = new FormData(); // Temporary storage (Use a DB instead)

    @PostMapping("/submit-form")
    public ResponseEntity<String> submitForm(@RequestBody FormData formData) {
        storedData = formData; // Save data (Replace this with database storage)
        return ResponseEntity.ok("Data received successfully!");
    }

    @GetMapping("/get-form-data")
    public ResponseEntity<FormData> getFormData() {
        return ResponseEntity.ok(storedData); // Send saved data
    }
}

