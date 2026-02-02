package com.example.jobportal.dto.request;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserRequest {
    private String fullName;
    private String email;
    private String password;
    private LocalDate dateOfBirth;
    private AddressRequest addressRequest;
    private String phoneNumber;
    private Boolean isEmailVerified = false;
}
