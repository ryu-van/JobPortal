package com.example.jobportal.service;

import com.example.jobportal.dto.response.UserBaseResponse;
import com.example.jobportal.dto.response.UserDetailResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public class UserServiceImpl implements UserService{
    @Override
    public Page<UserBaseResponse> getUsersByHrRole(String keyword, String companyName, Boolean isActive, Pageable pageable) {
        return null;
    }

    @Override
    public Page<UserBaseResponse> getUsersByAdminCompanyRole(String keyword, Boolean isActive, Pageable pageable) {
        return null;
    }

    @Override
    public Page<UserBaseResponse> getUsersByCandidateRole(String keyword, Boolean isActive, Pageable pageable) {
        return null;
    }

    @Override
    public Page<UserBaseResponse> getUsersByCompany(String keyword, Boolean isActive, Pageable pageable) {
        return null;
    }

    @Override
    public Page<UserBaseResponse> getHrUsersInCompany(String keyword, Boolean isActive, Pageable pageable) {
        return null;
    }

    @Override
    public UserDetailResponse getUserById(Long id) {
        return null;
    }

    @Override
    public UserDetailResponse updateUser(Long id, UserDetailResponse request) {
        return null;
    }

    @Override
    public void toggleUserActive(Long id, Boolean isActive) {

    }
}
