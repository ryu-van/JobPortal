package com.example.jobportal.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.MappedSuperclass;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@Embeddable
public  abstract  class BaseAddress {
    private String address;

    private String city;

    private String country;

}
