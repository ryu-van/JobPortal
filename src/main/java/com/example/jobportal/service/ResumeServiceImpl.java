package com.example.jobportal.service;

import com.example.jobportal.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ResumeServiceImpl implements ResumeService{
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final ResumeEducationRepository resumeEducationRepository;
    private final ResumeExperienceRepository resumeExperienceRepository;
    private final ResumeSkillRepository resumeSkillRepository;

}
