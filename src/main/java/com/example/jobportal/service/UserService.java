package com.example.jobportal.service;

import com.example.jobportal.dto.request.UpdateUserRequest;
import com.example.jobportal.dto.response.JobBaseResponseV2;
import com.example.jobportal.dto.response.UserBaseResponse;
import com.example.jobportal.dto.response.UserDetailResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService {

    // ---------------------- ADMIN SYSTEM ----------------------
    Page<UserBaseResponse> getUsersByHrRole(String keyword, String companyName, Boolean isActive, Pageable pageable);

    Page<UserBaseResponse> getUsersByAdminCompanyRole(String keyword, Boolean isActive, Pageable pageable);

    Page<UserBaseResponse> getUsersByCandidateRole(String keyword, Boolean isActive, Pageable pageable);

    // ---------------------- ADMIN COMPANY ----------------------
    List<UserBaseResponse> getHrUsersInCompany(String keyword, Boolean isActive, Long companyId,String asc);
    // ---------------------- COMMON ----------------------
    UserDetailResponse getUserById(Long id);
    UserBaseResponse updateUser(Long id, UpdateUserRequest request);
    void toggleUserActive(Long id, Boolean isActive);
    void deleteUser(Long id);
}
