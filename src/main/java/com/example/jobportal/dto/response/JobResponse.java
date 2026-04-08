package com.example.jobportal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class JobResponse {

    private Long id;

    private String title;

    private String company;

    private AddressResponse addressResponse;

    private String employmentType;

    private Integer numberOfPositions;

    private String status;

    private LocalDateTime publishedAt;

}
