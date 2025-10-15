package com.example.jobportal.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobBaseResponse {
    private Long id;
    private String title;
    private String companyName;
    private String address;
    private String companyLogo;
    private Boolean isSalaryNegotiable;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String salaryCurrency;
}
