package com.example.jobportal.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${spring.base-url}/test")
public class TestController {
    @GetMapping("/ping")
    public String ping() {
        return "OK";
    }

}
