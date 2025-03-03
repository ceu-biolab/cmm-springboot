package com.example.myapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example.myapp")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
