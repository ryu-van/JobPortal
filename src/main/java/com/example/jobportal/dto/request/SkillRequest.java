package com.example.jobportal.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillRequest {
    @NotBlank(message = "Tên kỹ năng không được để trống")
    private String name;

}
