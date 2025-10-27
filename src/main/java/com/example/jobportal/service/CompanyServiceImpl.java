package com.example.jobportal.service;


import com.example.jobportal.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CompanyServiceImpl implements CompanyService{
    private final CompanyRepository companyRepository;

}
