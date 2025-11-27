package com.example.jobportal.service;

import com.example.jobportal.dto.request.UpdateUserRequest;
import com.example.jobportal.dto.response.UserBaseResponse;
import com.example.jobportal.dto.response.UserDetailResponse;
import com.example.jobportal.exception.UserException;
import com.example.jobportal.model.entity.BaseAddress;
import com.example.jobportal.model.entity.Company;
import com.example.jobportal.model.entity.User;
import com.example.jobportal.model.enums.Role;
import com.example.jobportal.repository.CompanyRepository;
import com.example.jobportal.repository.RoleRepository;
import com.example.jobportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final RoleRepository roleRepository;
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
    public List<UserBaseResponse> getHrUsersInCompany(String keyword, Boolean isActive, Long companyId,String asc) {

        return userRepository.getUserInCompany(keyword,isActive,companyId,asc) ;
    }

    @Override
    public UserDetailResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->  UserException.notFound("User not found with id: " + id));
        return UserDetailResponse.fromEntity(user);
    }

    @Override
    public UserBaseResponse updateUser(Long id, UpdateUserRequest request) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() ->  UserException.notFound("User not found with id: " + id));
        Company existingCompany = companyRepository.findById(request.getCompanyId())
                .orElseThrow(()-> UserException.notFound("Company not found with id: " + request.getCompanyId()));
        com.example.jobportal.model.entity.Role existingRole = roleRepository.findById(request.getRoleId()).get();
        BaseAddress baseAddress = BaseAddress.builder()
                .street(request.getStreet())
                .ward(request.getWard())
                .district(request.getDistrict())
                .city(request.getCity())
                .country(request.getCountry())
                .build();
        existingUser.setFullName(request.getFullName());
        existingUser.setPhoneNumber(request.getPhoneNumber());
        existingUser.setAddress(baseAddress);
        existingUser.setAvatarUrl(request.getAvatarUrl());
        existingUser.setGender(request.getGender());
        existingUser.setCompany(existingCompany);
        existingUser.setRole(existingRole);
        User updatedUser = userRepository.save(existingUser);
        return UserBaseResponse.fromEntity(updatedUser);
    }

    @Override
    public void toggleUserActive(Long id, Boolean isActive) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() ->  UserException.notFound("User not found with id: " + id));
        existingUser.setIsActive(isActive);
        userRepository.save(existingUser);
    }

    @Override
    public void deleteUser(Long id) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> UserException.notFound("User not found with id: " + id));
        userRepository.delete(existingUser);
    }


}
