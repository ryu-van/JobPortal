package com.example.jobportal.controller;

import com.example.jobportal.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("${spring.base-url}/users")
public class UserController {
    private final UserService userService;
}
