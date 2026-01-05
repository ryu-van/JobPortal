package com.example.jobportal.controller;

import com.example.jobportal.dto.request.UpdateUserRequest;
import com.example.jobportal.dto.response.*;
import com.example.jobportal.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    @PutMapping("/status/{id}")
    public ResponseEntity<ApiResponse<Void>> toggleChangeStatus(@PathVariable("id") Long id, @RequestParam Boolean status) {
        userService.toggleUserActive(id, status);
        return ok("Change status account successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable("id") Long id) {
        userService.deleteUser(id);
        return ok("User deleted successfully");
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<ApiResponse<UserDetailResponse>> getDetailInformationOfUser(@PathVariable("id") Long id) {
        UserDetailResponse existingUser = userService.getUserById(id);
        return ok("Get detail information of user", existingUser);
    }

    @GetMapping("/hrs")
    public ResponseEntity<ApiResponse<List<UserBaseResponse>>> getListHrs(
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
        Page<UserBaseResponse> hrPages = userService.getUsersByHrRole(keyword, companyName, isActive, pageable);
        PageInfo pageInfo = PageInfo.of(hrPages.getNumber(), hrPages.getSize(), hrPages.getTotalElements());
        return ok("Get list of hr", hrPages.getContent(), pageInfo);
    }

    @GetMapping("/candidates")
    public ResponseEntity<ApiResponse<List<UserBaseResponse>>> getListCandidates(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "16") int size,
            @RequestParam(defaultValue = "asc") String asc
    ) {
        Sort sort = Sort.by("fullName");
        sort = asc.equalsIgnoreCase("asc") ? sort.ascending() : sort.descending();

        Pageable pageable = PageRequest.of(pageNo, size, sort);

        Page<UserBaseResponse> candidates =
                userService.getUsersByCandidateRole(keyword, isActive, pageable);
        PageInfo pageInfo = PageInfo.of(candidates.getNumber(), candidates.getSize(), candidates.getTotalElements());
        return ok("Get candidates", candidates.getContent(), pageInfo);
    }

    @GetMapping("/admin-company/users")
    public ResponseEntity<ApiResponse<List<UserBaseResponse>>> getUsersByAdminCompany(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "16") int size,
            @RequestParam(defaultValue = "asc") String asc
    ) {

        Sort sort = Sort.by("fullName");
        sort = asc.equalsIgnoreCase("asc") ? sort.ascending() : sort.descending();

        Pageable pageable = PageRequest.of(pageNo, size, sort);

        Page<UserBaseResponse> adminCompanyRoles =
                userService.getUsersByAdminCompanyRole(keyword, isActive, pageable);
        PageInfo pageInfo = PageInfo.of(adminCompanyRoles.getNumber(), adminCompanyRoles.getSize(), adminCompanyRoles.getTotalElements());
        return ok("Get admin companies", adminCompanyRoles.getContent(), pageInfo);
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
