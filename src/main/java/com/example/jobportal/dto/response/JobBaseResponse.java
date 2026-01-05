package com.example.jobportal.dto.response;


import com.example.jobportal.model.entity.Job;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class JobBaseResponse {
    private Long id;
    private String title;
    private String companyName;

    private AddressResponse address;
    private String companyLogo;

    private Boolean isSalaryNegotiable;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String salaryCurrency;



    public static JobBaseResponse fromEntity(Job job) {
        if (job == null) return null;
        return JobBaseResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .companyName(job.getCompany().getName())
                .address(AddressResponse.fromEntity(job.getAddress()))
                .companyLogo(job.getCompany().getLogoUrl())
                .isSalaryNegotiable(job.getIsSalaryNegotiable())
                .salaryMin(job.getSalaryMin())
                .salaryMax(job.getSalaryMax())
                .salaryCurrency(job.getSalaryCurrency())
                .build();
    }
}
