package com.example.jobportal.service;

import com.example.jobportal.dto.response.JobBaseResponseV2;
import com.example.jobportal.dto.response.UserBaseResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    Page<UserBaseResponse> getUsersByHrRole();
    Page<UserBaseResponse> getJobsByCompanyRole();
}
