package com.example.myapp.api;

import com.example.myapp.model.HelloUnit;
import com.example.myapp.service.HelloRuleEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rules")
public class HelloRuleController {

    private final HelloRuleEngine helloRuleEngine;

    @Autowired
    public HelloRuleController(HelloRuleEngine helloRuleEngine) {
        this.helloRuleEngine = helloRuleEngine;
    }

    @GetMapping("/hello")
    public ResponseEntity<?> addHelloAndRun(@RequestParam String hello) {
        return ResponseEntity.ok(helloRuleEngine.addHelloAndRun(hello));
    }
}
