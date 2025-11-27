package com.example.jobportal.controller;

import com.example.jobportal.dto.request.UpdateUserRequest;
import com.example.jobportal.dto.response.ApiResponse;
import com.example.jobportal.dto.response.PageInfo;
import com.example.jobportal.dto.response.UserBaseResponse;
import com.example.jobportal.dto.response.UserDetailResponse;
import com.example.jobportal.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("${spring.base-url}/users")
public class UserController extends BaseController {
    private final UserService userService;

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserBaseResponse>> updateInformationForUser(@PathVariable("id") Long id, @RequestBody UpdateUserRequest updateUserRequest){
        UserBaseResponse updatedUser = userService.updateUser(id,updateUserRequest);
        return ok("User updated successfully",updatedUser);

    }
    @PutMapping("/status/{id}")
    public ResponseEntity<ApiResponse<Void>> toggleChangeStatus(@PathVariable("id") Long id, @RequestParam Boolean status  ){
        userService.toggleUserActive(id, status);
        return ok("Change status account successfully");
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable("id") Long id ){
        userService.deleteUser(id);
        return ok("User deleted successfully");
    }
    @GetMapping("/detail/{id}")
    public ResponseEntity<ApiResponse<UserDetailResponse>> getDetailInformationOfUser(@PathVariable("id") Long id){
        UserDetailResponse existingUser = userService.getUserById(id);
        return ok("Get detail information of user",existingUser);
    }
    @GetMapping("/hrs")
    public ResponseEntity<ApiResponse<List<UserBaseResponse>>> getListHrs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "16") int size,
            @RequestParam(defaultValue = "asc") String asc
    ){
        Sort sort = Sort.by("fullName");
        sort = asc.equalsIgnoreCase("asc") ? sort.ascending() : sort.descending();
        Pageable pageable = PageRequest.of(pageNo, size, sort);
        Page<UserBaseResponse> hrPages = userService.getUsersByHrRole(keyword,companyName,isActive,pageable);
        PageInfo pageInfo = PageInfo.of(hrPages.getNumber(),hrPages.getSize(),hrPages.getTotalElements());
        return ok("Get list of hr",hrPages.getContent(),pageInfo);
    }
    @GetMapping("/candidates")
    public ResponseEntity<ApiResponse<Page<UserBaseResponse>>> getListCandidates(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "16") int size,
            @RequestParam(defaultValue = "asc") String asc
    ){
        Sort sort = Sort.by("fullName");
        sort = asc.equalsIgnoreCase("asc") ? sort.ascending() : sort.descending();

        Pageable pageable = PageRequest.of(pageNo, size, sort);

        Page<UserBaseResponse> data =
                userService.getUsersByCandidateRole(keyword, isActive, pageable);

        return ResponseEntity.ok(
                ApiResponse.success(data)
        );
    }
    @GetMapping("/admin-company/users")
    public ResponseEntity<ApiResponse<Page<UserBaseResponse>>> getUsersByAdminCompany(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "16") int size,
            @RequestParam(defaultValue = "asc") String asc
    ) {

        Sort sort = Sort.by("fullName");
        sort = asc.equalsIgnoreCase("asc") ? sort.ascending() : sort.descending();

        Pageable pageable = PageRequest.of(pageNo, size, sort);

        Page<UserBaseResponse> page =
                userService.getUsersByAdminCompanyRole(keyword, isActive, pageable);

        return ResponseEntity.ok(ApiResponse.success(page));
    }







}
