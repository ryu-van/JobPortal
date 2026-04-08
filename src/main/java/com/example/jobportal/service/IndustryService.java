package com.example.jobportal.service;

import com.example.jobportal.dto.request.IndustryRequest;
import com.example.jobportal.dto.response.IndustryResponse;
import com.example.jobportal.model.entity.Industry;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public interface IndustryService {
    List<IndustryResponse> getAllIndustry(String name);
    IndustryResponse createIndustry(IndustryRequest request);
    IndustryResponse updateIndustry(Long id, IndustryRequest request);
    IndustryResponse getIndustryById(Long id);
    void deleteIndustry(Long id);
    void changeStatusIndustry(Long id, Boolean isActive);
}
