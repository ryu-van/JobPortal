package com.example.jobportal.service;


import com.example.jobportal.dto.request.IndustryRequest;
import com.example.jobportal.dto.response.IndustryResponse;
import com.example.jobportal.exception.IndustryException;
import com.example.jobportal.model.entity.Industry;
import com.example.jobportal.repository.IndustryRepository;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class IndustryServiceImpl implements IndustryService {

    private final IndustryRepository industryRepository;


    @Override
    public List<IndustryResponse> getAllIndustry(String name) {
        List<Industry> industries;

        if (name == null || name.isBlank()) {
            industries = industryRepository.findAll();
        } else {
            industries = industryRepository.findByNameContainingIgnoreCase(name.trim());
        }

        return industries.stream()
                .map(IndustryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public IndustryResponse createIndustry(IndustryRequest request) {
        if (industryRepository.existsByCode(request.getCode())) {
            IndustryException.badRequest("Industry code already exists: " + request.getCode());
        }

        Industry industry = Industry.builder()
                .name(request.getName())
                .code(request.getCode())
                .description(request.getDescription())
                .isActive(request.getIsActive())
                .build();

        industryRepository.save(industry);
        return IndustryResponse.fromEntity(industry);
    }

    @Override
    public IndustryResponse updateIndustry(Long id, IndustryRequest request) {
        Industry industry = industryRepository.findById(id)
                .orElseThrow(() -> IndustryException.notFound("Industry not found with id: " + id));

        // Check if new code already exists (and it's not the same industry)
        if (!industry.getCode().equals(request.getCode()) &&
                industryRepository.existsByCode(request.getCode())) {
            IndustryException.badRequest("Industry code already exists: " + request.getCode());
        }

        industry.setName(request.getName());
        industry.setCode(request.getCode());
        industry.setDescription(request.getDescription());
        industry.setIsActive(request.getIsActive());

        industryRepository.save(industry);
        return IndustryResponse.fromEntity(industry);
    }

    @Override
    public IndustryResponse getIndustryById(Long id) {
        Industry industry = industryRepository.findById(id)
                .orElseThrow(() -> IndustryException.notFound("Industry not found with id: " + id));
        return IndustryResponse.fromEntity(industry);
    }

    @Override
    public void deleteIndustry(Long id) {
        Industry industry = industryRepository.findById(id)
                .orElseThrow(() -> IndustryException.notFound("Industry not found with id: " + id));
        industryRepository.delete(industry);

    }

    @Override
    public void changeStatusIndustry(Long id, Boolean isActive) {
        Industry industry = industryRepository.findById(id)
                .orElseThrow(() -> IndustryException.notFound("Industry not found with id: " + id));
        industry.setIsActive(isActive);
        industryRepository.save(industry);

    }
}
