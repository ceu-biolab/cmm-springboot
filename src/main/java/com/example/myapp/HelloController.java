package com.example.myapp;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.stereotype.Controller;

@Controller
public class HelloController {

    @GetMapping("/")
    public String mainWebSearch() {
        return "main-web.html";
    }

    @GetMapping("/main-web.html")
    public String mainSearch() {
        return "main-web.html";
    }


    @GetMapping("/batch-search.html")
    public String batchSearch() {
        return "batch-search.html";
    }

    @GetMapping("/simple-search.html")
    public String simpleSearch() {
        return "simple-search.html";
    }



}
