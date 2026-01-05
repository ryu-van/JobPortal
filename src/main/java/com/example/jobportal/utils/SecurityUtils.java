package com.example.jobportal.utils;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.jobportal.security.CustomUserDetails;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static CustomUserDetails currentUser() {
        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails)) {
            throw new AccessDeniedException("Unauthenticated");
        }

        return (CustomUserDetails) auth.getPrincipal();
    }

    public static Long currentUserId() {
        return currentUser().getId();
    }
}
