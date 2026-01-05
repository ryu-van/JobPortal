package com.example.jobportal.dto.response;

import com.example.jobportal.model.entity.Address;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {
    private String addressType;
    private String provinceCode;
    private String provinceName;
    private String communeCode;
    private String communeName;
    private String detailAddress;
    private Boolean isPrimary;

    public static AddressResponse fromEntity(Address address) {
        if (address == null) return null;
        return AddressResponse.builder()
                .addressType(address.getAddressType())
                .provinceCode(address.getProvinceCode())
                .provinceName(address.getProvinceName())
                .communeCode(address.getCommuneCode())
                .communeName(address.getCommuneName())
                .detailAddress(address.getDetailAddress())
                .isPrimary(address.getIsPrimary())
                .build();
    }
}
