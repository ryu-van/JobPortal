package com.example.jobportal.model.entity;

import com.example.jobportal.dto.request.AddressRequest;
import com.example.jobportal.dto.request.UpdateCompanyRequest;
import org.springframework.stereotype.Component;


@Component
public class AddressHelper {


    public Address build(AddressRequest request) {
        Address address = new Address();
        map(address, request);
        address.setIsPrimary(Boolean.TRUE.equals(request.getIsPrimary()));
        address.setIsActive(true);
        return address;
    }
    

    public void update(Address address, AddressRequest request) {
        map(address, request);
        if (request.getIsPrimary() != null) {
            address.setIsPrimary(request.getIsPrimary());
        }
    }
    private void map(Address address, AddressRequest request) {
        address.setAddressType(request.getAddressType());
        address.setProvinceCode(request.getProvinceCode());
        address.setProvinceName(request.getProvinceName());
        address.setCommuneCode(request.getCommuneCode());
        address.setCommuneName(request.getCommuneName());
        address.setDetailAddress(request.getDetailAddress());
    }
}

