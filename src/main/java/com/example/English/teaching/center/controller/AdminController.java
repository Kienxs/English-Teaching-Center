package com.example.English.teaching.center.controller;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.English.teaching.center.model.BlogPost;
import com.example.English.teaching.center.service.AdminService;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }   

    @GetMapping("/dashboard")
    public String showAdminDashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/posts/approve")
    public String showPendingPosts(Model model,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size){
                          
        Page<BlogPost> blogPage = adminService.getPendingBlogs(page, size);
        model.addAttribute("blogPage", blogPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", blogPage.getTotalPages());

        return "admin/approve_posts";
    }

    @GetMapping("/courses/approve")
    public String showPendingCourses(Model model,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size){
                          
        Page<com.example.English.teaching.center.model.Course> coursePage = adminService.getPendingCourse(page, size);
        model.addAttribute("coursePage", coursePage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", coursePage.getTotalPages());

        return "admin/approve_courses";
    }

    @GetMapping("/finance/reports")
    public String showFinanceReport(){
        return "admin/finance-reports";
    }
}
