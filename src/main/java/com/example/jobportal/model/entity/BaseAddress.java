package com.example.jobportal.model.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Embeddable
public class BaseAddress {
    private String address;
    private String city;
    private String country;
}