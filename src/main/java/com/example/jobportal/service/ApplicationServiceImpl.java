package com.example.jobportal.service;

import com.example.jobportal.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ApplicationServiceImpl {
    private final ApplicationRepository applicationRepository;
}
