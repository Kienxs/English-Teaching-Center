package com.example.English.teaching.center.controller.admin;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.English.teaching.center.service.course.CourseService;
import com.example.English.teaching.center.service.user.AdminService;

import com.example.English.teaching.center.entity.Course;

@Controller
@RequestMapping("/admin")
public class AdminController {

//     private final AdminService adminService;
//     private final CourseService courseService;

//     public AdminController(AdminService adminService,
//                             CourseService courseService) {
//         this.adminService = adminService;
//         this.courseService = courseService;
//     }   

//     @GetMapping("/dashboard")
//     public String showAdminDashboard() {
//         return "admin/dashboard";
//     }

//     @GetMapping("/posts/approve")
//     public String showPendingPosts(Model model,
//                                 @RequestParam(defaultValue = "0") int page,
//                                 @RequestParam(defaultValue = "10") int size){
                          
//         Page<BlogPost> blogPage = adminService.getPendingBlogs(page, size);
//         model.addAttribute("blogPage", blogPage);
//         model.addAttribute("currentPage", page);
//         model.addAttribute("totalPages", blogPage.getTotalPages());

//         return "admin/approve_posts";
//     }

// // Flow for course approval -------------------------------------------------------s
//     @GetMapping("/courses/approve")
//     public String showPendingCourses(Model model,
//                                 @RequestParam(defaultValue = "0") int page,
//                                 @RequestParam(defaultValue = "10") int size){
                          
//         Page<Course> coursePage = adminService.getPendingCourse(page, size);
//         model.addAttribute("coursePage", coursePage);
//         model.addAttribute("currentPage", page);
//         model.addAttribute("totalPages", coursePage.getTotalPages());

//         return "admin/approve_courses";
//     }

//     @GetMapping("courses/preview/{id}")
//     public String previewCourse(@PathVariable Long id, Model model){
//         Course course = courseService.findCourseById(id).orElse(null);
//         model.addAttribute("course", course);
//         return "admin/preview_course";
//     }

//     @PostMapping("courses/approve/{id}")
//     public String approveCourse(@PathVariable Long id) {
//         adminService.approveCourse(id);
//         return "redirect:/admin/courses/approve?status=approved";
//     }

//     @PostMapping("courses/reject/{id}")
//     public String rejectCourse(@PathVariable Long id,
//                             @RequestParam String note) {
//         adminService.rejectCourse(id, note);
//         return "redirect:/admin/courses/approve?status=rejected";
//     }

//     @GetMapping("/finance/reports")
//     public String showFinanceReport(){
//         return "admin/finance-reports";
    // }
}
