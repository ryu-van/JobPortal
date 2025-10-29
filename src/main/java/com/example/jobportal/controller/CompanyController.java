package com.example.jobportal.controller;

import com.example.jobportal.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("${spring.base-url}/companies")
public class CompanyController {
    private final CompanyService companyService;
}
