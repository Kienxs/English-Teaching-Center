package com.example.English.teaching.center.model.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.example.English.teaching.center.model.StudentCourse;

import lombok.Data;

@Data
public class TeacherDashboardDTO {
    // KPI
    private Long totalViews;
    private Long totalCourses;
    private Long totalStudents;
    private BigDecimal totalRevenue;

    // Lists
    private List<Map<String, Object>> topCourses;
    private List<StudentCourse> recentEnrollments;

    // Chart Data
    private List<String> chartLabels;
    private List<Long> chartData;
}
