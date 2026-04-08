package com.example.jobportal.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCompanyVerificationRequest {
    private Long reviewedById;
    @JsonProperty("isApproved")
    @JsonAlias({"approved", "isApproved"})
    private boolean approved;
    private String reason;
}
