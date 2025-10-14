package com.example.jobportal.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public  abstract  class BaseAddress {
    private String address;

    private String city;

    private String country;

}
