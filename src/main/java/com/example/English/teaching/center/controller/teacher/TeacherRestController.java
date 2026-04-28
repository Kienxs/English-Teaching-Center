package com.example.English.teaching.center.controller.teacher;

import com.example.English.teaching.center.service.user.TeacherService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/teacher/api")
public class TeacherRestController {

    private final TeacherService teacherService;

    @GetMapping("/revenue-chart")
    public Map<String, Object> getRevenueChartData(@RequestParam String range, 
                                                Principal principal) {
        return teacherService.getRevenueChartData(principal.getName(), range);
    }
}