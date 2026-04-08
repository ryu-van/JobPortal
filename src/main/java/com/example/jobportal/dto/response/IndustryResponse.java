package com.example.jobportal.dto.response;

import com.example.jobportal.model.entity.Industry;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndustryResponse {

    private Long id;

    private String code;

    private String name;

    private String description;

    private Boolean isActive;

    public static IndustryResponse fromEntity(Industry industry) {
        return IndustryResponse.builder().id(industry.getId())
                .code(industry.getCode()).name(industry.getName())
                .description(industry.getDescription()).isActive(industry.getIsActive()).build();
    }

}
