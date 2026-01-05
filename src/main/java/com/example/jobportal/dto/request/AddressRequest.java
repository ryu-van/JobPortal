package com.example.jobportal.dto.request;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {
    private String addressType;
    private String provinceCode;
    private String provinceName;
    private String communeCode;
    private String communeName;
    private String detailAddress;
    private Boolean isPrimary;
}
