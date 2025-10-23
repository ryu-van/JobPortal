package com.example.jobportal.dto.response;

import com.example.jobportal.model.entity.JobCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class JobCategoryResponse {
    private Long id;
    private String name;
    private String description;
    private Long parentId;
    private String parentName;
    public JobCategoryResponse fromEntity(JobCategory category) {
        JobCategoryResponse response = new JobCategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        if (category.getParentCategory() != null) {
            response.setParentId(category.getParentCategory().getId());
            response.setParentName(category.getParentCategory().getName());
        }
        return response;
    }

}
