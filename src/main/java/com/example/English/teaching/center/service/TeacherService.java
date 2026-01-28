package com.example.English.teaching.center.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.English.teaching.center.model.User;
import com.example.English.teaching.center.model.dto.TeacherDashboardDTO;
import com.example.English.teaching.center.repository.CourseRepository;
import com.example.English.teaching.center.repository.StudentCourseRepository;
import com.example.English.teaching.center.repository.UserRepository;

@Service
public class TeacherService {
    private final CourseRepository courseRepository;
    private final StudentCourseRepository studentCourseRepository;
    private final UserRepository userRepository;

    public TeacherService(CourseRepository courseRepository,
                          StudentCourseRepository studentCourseRepository,
                          UserRepository userRepository) {
        this.courseRepository = courseRepository;
        this.studentCourseRepository = studentCourseRepository;
        this.userRepository = userRepository;
    }

    public TeacherDashboardDTO getDashboardData(String email){
        User teacher = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Teacher not found"));
        Long teacherId = teacher.getId();   

        TeacherDashboardDTO dto = new TeacherDashboardDTO();

        // 1.Logic frocess KPI & Null check
        Long totalViews = courseRepository.sumViewByTEacherId(teacherId);
        dto.setTotalViews(totalViews != null ? totalViews : 0L);

        Long totalCourses = courseRepository.countByTeacherId(teacherId);
        dto.setTotalCourses(totalCourses != null ? totalCourses : 0L);

        Long totalStudents = studentCourseRepository.countTotalStudentByTeacher(teacherId);
        dto.setTotalStudents(totalStudents);

        BigDecimal totalRevenue = studentCourseRepository.calculateTotalRevenueByTeacherId(teacherId);
        dto.setTotalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO);

        // 2. Get the list
        dto.setTopCourses(courseRepository.findTopSellingCourses(teacherId));
        dto.setRecentEnrollments(studentCourseRepository.findRecentErollments(teacherId));

        // 3. Logic for processing chart data (Converting raw data into a List for FE)
        List<Object[]> categoryStats = studentCourseRepository.countSalesByCategory(teacherId);
        List<String> labels = new ArrayList<>();
        List<Long> data = new ArrayList<>();

        if(categoryStats != null){
            for(Object[] row : categoryStats){
                String label = row[0] != null ? row[0].toString() : "Unknown";
                Long count = row[1] != null ? (Long) row[1] : 0L;
                labels.add(label);
                data.add(count);
            }
        }

        dto.setChartLabels(labels);
        dto.setChartData(data);

        return dto;
    }
}
