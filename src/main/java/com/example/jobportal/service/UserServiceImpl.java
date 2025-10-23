package com.example.jobportal.service;

import com.example.jobportal.dto.response.UserBaseResponse;
import com.example.jobportal.dto.response.UserDetailResponse;
import com.example.jobportal.model.entity.User;
import com.example.jobportal.model.enums.Role;
import com.example.jobportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;
    @Override
    public Page<UserBaseResponse> getUsersByHrRole(String keyword, String companyName, Boolean isActive, Pageable pageable) {
        return userRepository.getUserByHrRole(keyword, companyName, isActive, pageable);
    }

    @Override
    public Page<UserBaseResponse> getUsersByAdminCompanyRole(String keyword, Boolean isActive, Pageable pageable) {
        return userRepository.getUserByRole(keyword, isActive, pageable, (long) Role.ROLE_COMPANY_ADMIN.getId());
    }

    @Override
    public Page<UserBaseResponse> getUsersByCandidateRole(String keyword, Boolean isActive, Pageable pageable) {
        return userRepository.getUserByRole(keyword, isActive, pageable, (long) Role.ROLE_CANDIDATE.getId());
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
    public UserBaseResponse updateUser(Long id, UserDetailResponse request) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        return null;
    }

    @Override
    public void toggleUserActive(Long id, Boolean isActive) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        existingUser.setIsActive(isActive);
        userRepository.save(existingUser);
    }


}
