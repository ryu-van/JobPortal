package com.example.jobportal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class JobBaseResponseV2 extends JobBaseResponse{
    private String workType;
    private String employmentType;
    private String experienceLevel;
    private Integer numberOfPositions;
    private LocalDateTime applicationDeadline;
    private List<String> getCategoriesName;
}
