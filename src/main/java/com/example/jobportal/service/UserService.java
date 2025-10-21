package com.example.jobportal.service;

import com.example.jobportal.dto.response.JobBaseResponseV2;
import com.example.jobportal.dto.response.UserBaseResponse;
import com.example.jobportal.dto.response.UserDetailResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public interface UserService {

    // ---------------------- ADMIN SYSTEM ----------------------
    Page<UserBaseResponse> getUsersByHrRole(String keyword, String companyName, Boolean isActive, Pageable pageable);

    Page<UserBaseResponse> getUsersByAdminCompanyRole(String keyword, Boolean isActive, Pageable pageable);

    Page<UserBaseResponse> getUsersByCandidateRole(String keyword, Boolean isActive, Pageable pageable);

    // ---------------------- ADMIN COMPANY ----------------------
    Page<UserBaseResponse> getUsersByCompany(String keyword, Boolean isActive, Pageable pageable);

    Page<UserBaseResponse> getHrUsersInCompany(String keyword, Boolean isActive, Pageable pageable);
    // ---------------------- COMMON ----------------------
    UserDetailResponse getUserById(Long id);
    UserDetailResponse updateUser(Long id, UserDetailResponse request);
    void toggleUserActive(Long id, Boolean isActive);

}
