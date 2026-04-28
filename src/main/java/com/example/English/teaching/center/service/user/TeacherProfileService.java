package com.example.English.teaching.center.service.user;

import org.springframework.stereotype.Service;

import com.example.English.teaching.center.dto.report.TeacherProfileRequest;
import com.example.English.teaching.center.dto.report.TeacherProfileResponse;
import com.example.English.teaching.center.entity.Teacher;
import com.example.English.teaching.center.entity.User;
import com.example.English.teaching.center.exception.RateLimitException;
import com.example.English.teaching.center.repository.TeacherRepository;
import com.example.English.teaching.center.repository.UserRepository;
import com.example.English.teaching.center.service.infra.RateLimitingService;

import io.github.bucket4j.Bucket;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TeacherProfileService {
    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;
    private final RateLimitingService rateLimitingService;

    public TeacherProfileResponse getExpertiseProfile(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với email: " + email));

        TeacherProfileResponse dto = new TeacherProfileResponse();

        teacherRepository.findById(user.getId()).ifPresent(teacher -> {
            dto.setBio(teacher.getBio());
            dto.setExpertise(teacher.getExpertise());
            dto.setLinkedinUrl(teacher.getLinkedinUrl());
        });

        return dto;
    }

    @Transactional
    public void updateExpertiseProfile(String email, TeacherProfileRequest dto) {
        String limitKey = "UPDATE_TEACHER_" + email;

        Bucket bucket = rateLimitingService.resolveBucket(limitKey, 5, 10);
        if(!bucket.tryConsume(1))
            throw new RateLimitException("Hệ thống đang bận, vui lòng thao tác chậm lại!");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy User"));

        Teacher teacher = teacherRepository.findById(user.getId()).orElseGet(() -> {
            Teacher newTeacher = new Teacher();
            newTeacher.setId(user.getId()); 
            newTeacher.setUser(user);
            return newTeacher;
        });

        teacher.setBio(dto.getBio());
        teacher.setExpertise(dto.getExpertise());
        teacher.setLinkedinUrl(dto.getLinkedinUrl());

        teacherRepository.save(teacher); 
    }
}