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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final RoleRepository roleRepository;
    private final FileUploadService fileUploadService;

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
    public List<UserBaseResponse> getHrUsersInCompany(String keyword, Boolean isActive, Long companyId, String asc) {

        return userRepository.getUserInCompany(keyword, isActive, companyId, asc);
    }

    @Override
    public UserDetailResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> UserException.notFound("User not found with id: " + id));
        return UserDetailResponse.fromEntity(user);
    }

    @Override
    public UserBaseResponse updateUser(Long id, UpdateUserRequest request) {

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> UserException.notFound("User not found with id: " + id));

        if (request.getCompanyId() != null) {
            Company existingCompany = companyRepository.findById(request.getCompanyId())
                    .orElseThrow(() -> UserException.notFound("Company not found with id: " + request.getCompanyId()));
            existingUser.setCompany(existingCompany);
        }

        if (request.getRoleId() != null) {
            com.example.jobportal.model.entity.Role existingRole = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> UserException.notFound("Role not found with id: " + request.getRoleId()));
            existingUser.setRole(existingRole);
        }

        BaseAddress baseAddress = BaseAddress.builder()
                .street(request.getStreet() != null ? request.getStreet() : existingUser.getAddress().getStreet())
                .ward(request.getWard() != null ? request.getWard() : existingUser.getAddress().getWard())
                .district(request.getDistrict() != null ? request.getDistrict() : existingUser.getAddress().getDistrict())
                .city(request.getCity() != null ? request.getCity() : existingUser.getAddress().getCity())
                .country(request.getCountry() != null ? request.getCountry() : existingUser.getAddress().getCountry())
                .build();

        existingUser.setAddress(baseAddress);

        if (request.getFullName() != null)
            existingUser.setFullName(request.getFullName());

        if (request.getPhoneNumber() != null)
            existingUser.setPhoneNumber(request.getPhoneNumber());

        if (request.getGender() != null)
            existingUser.setGender(request.getGender());

        if (request.getIsActive() != null)
            existingUser.setIsActive(request.getIsActive());

        User updatedUser = userRepository.save(existingUser);

        return UserBaseResponse.fromEntity(updatedUser);
    }

    @Override
    public void toggleUserActive(Long id, Boolean isActive) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> UserException.notFound("User not found with id: " + id));
        existingUser.setIsActive(isActive);
        userRepository.save(existingUser);
    }

    @Override
    public void deleteUser(Long id) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> UserException.notFound("User not found with id: " + id));
        userRepository.delete(existingUser);
    }

    @Override
    public String updateAvatar(Long id, MultipartFile file) {
        User existingUser = userRepository.findById(id).orElseThrow(() -> UserException.notFound("User not found with id: " + id));
        String fileName = fileUploadService.uploadFile(file);
        existingUser.setAvatarUrl(fileName);
        userRepository.save(existingUser);
        return existingUser.getAvatarUrl();
    }


}
