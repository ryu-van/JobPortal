package com.example.jobportal.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public class JobBaseResponseV2 extends JobBaseResponse{
    private String workType;
    private String employmentType;
    private String experienceLevel;
    private Integer numberOfPositions;
    private LocalDateTime applicationDeadline;
    private List<String> getCategoriesName;
}
