package com.example.jobportal.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.jobportal.constant.AppConstants;
import com.example.jobportal.dto.request.CreateUserRequest;
import com.example.jobportal.dto.request.UpdateUserRequest;
import com.example.jobportal.dto.response.UploadResultResponse;
import com.example.jobportal.dto.response.UserBaseResponse;
import com.example.jobportal.dto.response.UserDetailResponse;
import com.example.jobportal.exception.UserException;
import com.example.jobportal.model.entity.AddressHelper;
import com.example.jobportal.model.entity.Company;
import com.example.jobportal.model.entity.User;
import com.example.jobportal.model.enums.Gender;
import com.example.jobportal.model.enums.Role;
import com.example.jobportal.model.enums.UploadType;
import com.example.jobportal.repository.CompanyRepository;
import com.example.jobportal.repository.RoleRepository;
import com.example.jobportal.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final RoleRepository roleRepository;
    private final FileUploadService fileUploadService;
    private final AddressHelper addressHelper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserBaseResponse createUser(CreateUserRequest createUserRequest, MultipartFile file) {
        if (userRepository.existsByEmail(createUserRequest.getEmail())) {
            throw UserException.badRequest("Email đã tồn tại");
        }

        User newUser = User.builder()
                .fullName(createUserRequest.getFullName())
                .email(createUserRequest.getEmail())
                .passwordHash(passwordEncoder.encode(createUserRequest.getPassword()))
                .phoneNumber(createUserRequest.getPhoneNumber())
                .dateOfBirth(createUserRequest.getDateOfBirth())
                .gender(createUserRequest.getGender() != null ? Gender.fromValue(createUserRequest.getGender()) : null)
                .code(generateUserCode())
                .isActive(true)
                .isEmailVerified(createUserRequest.getIsEmailVerified())
                .build();

        if (createUserRequest.getAddressRequest() != null) {
            newUser.setAddress(addressHelper.build(createUserRequest.getAddressRequest()));
        }

        com.example.jobportal.model.entity.Role existingRole = null;
        if (createUserRequest.getRoleId() != null) {
            existingRole = roleRepository.findById(createUserRequest.getRoleId()).orElse(null);
        }
        newUser.setRole(existingRole);

        if (file != null && !file.isEmpty()) {
            UploadResultResponse uploadResultResponse = fileUploadService.uploadSingle(file, UploadType.IMAGES);
            if ("SUCCESS".equals(uploadResultResponse.getStatus())) {
                newUser.setAvatarUrl(uploadResultResponse.getUrl());
                newUser.setAvatarPublicId(uploadResultResponse.getPublicId());
            } else {
                throw new RuntimeException("Upload failed: " + uploadResultResponse.getError());
            }
        }
        userRepository.save(newUser);
        return UserBaseResponse.fromEntity(newUser);
    }

    private String generateUserCode() {
        String code;
        do {
            code = String.format("USER-%s", java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        } while (userRepository.existsByCode(code));
        return code;
    }

    @Override
    public Page<UserBaseResponse> getUsersByRole(
            String role,
            String keyword,
            String companyName,
            Boolean isActive,
            Pageable pageable
    ) {
        String normalized = role == null ? null : role.trim().toUpperCase();
        String keywordPattern = toLikePatternLower(keyword);
        String companyPattern = toLikePatternLower(companyName);

        Long roleId = null;

        if (normalized == null || "ALL".equals(normalized)) {
            roleId = null;
        } else if (AppConstants.ROLE_HR.equals(normalized)) {
            roleId = (long) Role.ROLE_HR.getId();
        } else if (AppConstants.ROLE_CANDIDATE.equals(normalized)) {
            roleId = (long) Role.ROLE_CANDIDATE.getId();
        } else if (AppConstants.ROLE_COMPANY_ADMIN.equals(normalized)) {
            roleId = (long) Role.ROLE_COMPANY_ADMIN.getId();
        } else if (AppConstants.ROLE_ADMIN.equals(normalized)) {
            roleId = (long) Role.ROLE_ADMIN.getId();
        } else {
            throw new IllegalArgumentException("Invalid role value. Allowed: ALL, HR, CANDIDATE, COMPANY_ADMIN, ADMIN");
        }

        return userRepository.getUsersByRole(
                roleId,
                keywordPattern,
                companyPattern,
                isActive,
                pageable
        );
    }

    private static String toLikePatternLower(String s) {
        if (s == null || s.isBlank()) return null;
        return "%" + s.trim().toLowerCase() + "%";
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
    @Transactional
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

        if (request.getAddressRequest() != null) {
            if (existingUser.getAddress() == null) {
                existingUser.setAddress(addressHelper.build(request.getAddressRequest()));
            } else {
                addressHelper.update(existingUser.getAddress(), request.getAddressRequest());
            }
        }

        if (request.getFullName() != null)
            existingUser.setFullName(request.getFullName());

        if (request.getDateOfBirth() != null)
            existingUser.setDateOfBirth(request.getDateOfBirth());

        if (request.getPhoneNumber() != null)
            existingUser.setPhoneNumber(request.getPhoneNumber());

        if (request.getGender() != null)
            existingUser.setGender(Gender.fromValue(request.getGender()));

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
    @Transactional
    public void updateAvatar(Long id, MultipartFile file) {
        if(file== null || file.isEmpty()) {
            throw UserException.badRequest("File is required");
        }
        User existingUser = userRepository.findById(id).orElseThrow(() -> UserException.notFound("User not found with id: " + id));
        UploadResultResponse result = fileUploadService.replaceFile(
                file,
                existingUser.getAvatarPublicId(),
                UploadType.IMAGES
        );

        if ("SUCCESS".equals(result.getStatus())) {
            existingUser.setAvatarUrl(result.getUrl());
            existingUser.setAvatarPublicId(result.getPublicId());
            userRepository.save(existingUser);
        } else {
            throw new RuntimeException("Upload failed: " + result.getError());
        }
    }
    @Override
    @Transactional
    public void deleteAvatar(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserException.notFound("User not found"));
        if (user.getAvatarPublicId() != null) {
            fileUploadService.deleteFile(user.getAvatarPublicId());
        }
        user.setAvatarUrl(null);
        user.setAvatarPublicId(null);
        userRepository.save(user);
    }




}
