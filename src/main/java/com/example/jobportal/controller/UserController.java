package com.example.jobportal.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.jobportal.dto.request.UpdateUserRequest;
import com.example.jobportal.dto.response.ApiResponse;
import com.example.jobportal.dto.response.PageInfo;
import com.example.jobportal.dto.response.UserBaseResponse;
import com.example.jobportal.dto.response.UserDetailResponse;
import com.example.jobportal.service.UserService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("${spring.base-url}/users")
public class UserController extends BaseController {
    private final UserService userService;

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserBaseResponse>> updateInformationForUser(@PathVariable("id") Long id, @RequestBody UpdateUserRequest updateUserRequest) {
        UserBaseResponse updatedUser = userService.updateUser(id, updateUserRequest);
        return ok("User updated successfully", updatedUser);
    }

    @PutMapping(value = "/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Void>> updateAvatar(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) {
        userService.updateAvatar(id, file);
        return ok("Avatar updated successfully");
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Void>> toggleChangeStatus(@PathVariable("id") Long id, @RequestParam Boolean status) {
        userService.toggleUserActive(id, status);
        return ok("Change status account successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable("id") Long id) {
        userService.deleteUser(id);
        return ok("User deleted successfully");
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDetailResponse>> getDetailInformationOfUser(@PathVariable("id") Long id) {
        UserDetailResponse existingUser = userService.getUserById(id);
        return ok("Get detail information of user", existingUser);
    }


    @GetMapping
    public ResponseEntity<ApiResponse<List<UserBaseResponse>>> getUsersByRoleParam(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "16") int size,
            @RequestParam(defaultValue = "asc") String asc
    ) {
        Sort sort = Sort.by("fullName");
        sort = asc.equalsIgnoreCase("asc") ? sort.ascending() : sort.descending();
        Pageable pageable = PageRequest.of(pageNo, size, sort);

        Page<UserBaseResponse> page = userService.getUsersByRole(role, keyword, companyName, isActive, pageable);
        PageInfo pageInfo = PageInfo.of(page.getNumber(), page.getSize(), page.getTotalElements());
        return ok("Get users by role", page.getContent(), pageInfo);
    }

    @GetMapping("/{companyId}/users")
    public ResponseEntity<ApiResponse<List<UserBaseResponse>>> getUsersInCompany(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isActive,
            @PathVariable("companyId") Long companyId,
            @RequestParam(required = false) String asc
    ) {
        List<UserBaseResponse> listUserInCompany = userService.getHrUsersInCompany(keyword, isActive, companyId, asc);
        return ok("Get list of user in the company", listUserInCompany);
    }


}
