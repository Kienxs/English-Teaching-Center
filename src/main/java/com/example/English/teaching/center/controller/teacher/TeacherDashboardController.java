package com.example.English.teaching.center.controller.teacher;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.English.teaching.center.dto.TeacherDashboardDTO;
import com.example.English.teaching.center.service.user.TeacherService;

@Controller
@RequestMapping("/teacher")
public class TeacherDashboardController {
    private final TeacherService teacherService;

    public TeacherDashboardController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {

        TeacherDashboardDTO dashboardData = teacherService.getDashboardData(principal.getName());

        model.addAttribute("totalViews", dashboardData.getTotalViews());
        model.addAttribute("totalCourses", dashboardData.getTotalCourses());
        model.addAttribute("totalStudents", dashboardData.getTotalStudents());
        model.addAttribute("totalRevenue", dashboardData.getTotalRevenue());

        model.addAttribute("topCourses", dashboardData.getTopCourses());
        model.addAttribute("recentEnrollments", dashboardData.getRecentEnrollments());
        
        model.addAttribute("catLabels", dashboardData.getChartLabels());
        model.addAttribute("catData", dashboardData.getChartData());

        return "teacher/dashboard";
    }
}
