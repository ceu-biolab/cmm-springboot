package com.example.myapp;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.stereotype.Controller;

@Controller
public class HelloController {

    @GetMapping("/")
    public String mainWebSearch() {
        return "main-web";
    }

    @GetMapping("/batch_search")
    public String batchSearch() {
        return "batch-search";
    }

    @GetMapping("/simple_search")
    public String simpleSearch() {
        return "simple-search";
    }



}
