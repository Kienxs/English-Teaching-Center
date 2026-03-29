package com.example.English.teaching.center.controller.user;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.ui.Model;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.English.teaching.center.dto.CommentDTO;
import com.example.English.teaching.center.dto.TestDTO;
import com.example.English.teaching.center.entity.Course;
import com.example.English.teaching.center.entity.StudentCourse;
import com.example.English.teaching.center.entity.TestResult;
import com.example.English.teaching.center.entity.User;
import com.example.English.teaching.center.service.course.CourseCommentService;
import com.example.English.teaching.center.service.course.CourseService;
import com.example.English.teaching.center.service.course.TestService;
import com.example.English.teaching.center.service.user.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserCourseController {
    private final CourseService courseService;
    private final CourseCommentService courseCommentService;
    private final TestService testService;
    private final UserService userService;

    public UserCourseController(CourseService courseService, 
                                CourseCommentService courseCommentService,
                                TestService testService,
                                UserService userService) {
        this.courseService = courseService;
        this.courseCommentService = courseCommentService;
        this.testService = testService;
        this.userService = userService;
    }

    @GetMapping("/course-detail/{slug}")
    public String courseDetail(@PathVariable String slug, 
                            Model model, Principal principal,
                            HttpSession session){ 
        String email = (principal != null) ? principal.getName() : "anonymousUser";
        
        Map<String, Object> data = courseService.getCourseDetailData(slug, email, session);

        Course course = (Course) data.get("course");
        Page<CommentDTO> pageComments = courseCommentService.getCommentsByCourseId(course.getId(), 0, 5);
        
        model.addAttribute("courseComments", pageComments.getContent());
        model.addAttribute("totalComments", pageComments.getTotalElements());
        model.addAllAttributes(data);
        return "user/course-detail";
    }

    @GetMapping("/my-courses") 
    public ResponseEntity<List<Course>> getMyEnrolledCourses(Principal principal,
                        @RequestParam(name = "status", defaultValue = "ENROLLED") String statusString) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String email = principal.getName();
        User currentUser = userService.findByEmail(email);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        Long currentStudentId = currentUser.getId();

        StudentCourse.Status status;
        try {
            status = StudentCourse.Status.valueOf(statusString.toUpperCase());
        } catch (IllegalArgumentException e) {
            status = StudentCourse.Status.ENROLLED;
        }

        List<Course> courses = courseService.getCoursesByStudentAndStatus(currentStudentId, status);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/my-course-detail/{courseSlug}")
    public String myCoursesDetail(@PathVariable String courseSlug, 
                                @RequestParam(name = "lessonId", required = false) Long lessonId,
                                @RequestParam(name = "testSlug", required = false) String testSlug,
                                Model model, 
                                Principal principal) {

        if (principal == null) return "redirect:/login";

        User currentUser = userService.findByEmail(principal.getName());
        Map<String, Object> data = courseService.getMyCourseDetailData(courseSlug, lessonId, testSlug, currentUser.getId());

        model.addAllAttributes(data);
        return "user/my-course-detail"; 
    }

    @PostMapping("/course-detail/comment")
    public String postComment(@RequestParam("courseSlug") String courseSlug,
                            @RequestParam("commentText") String text,
                            Principal principal) {
        if (principal != null && text != null && !text.trim().isEmpty()) {
            courseService.findBySlug(courseSlug).ifPresent(course -> {
                courseCommentService.saveComment(course.getId(), principal.getName(), text);
            });
        }
        return "redirect:/user/course-detail/" + courseSlug + "#tab-binhluan";
    }

    @GetMapping("/do-test/{testSlug}")
    public String doTest(@PathVariable String testSlug, 
                        Model model, 
                        Principal principal,
                        RedirectAttributes ra) {
        
        if (principal == null) return "redirect:/login";
        
        try {
            User user = userService.findByEmail(principal.getName());
            TestResult result = testService.startOrResumeTest(testSlug, user);

            TestDTO safeTest = testService.getSafeTestDetails(testSlug);

            model.addAttribute("test", safeTest);
            model.addAttribute("resultId", result.getId());
            return "user/do-test";
        } catch (RuntimeException e) { 
            e.printStackTrace();
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/user/home";
        }
    }

    @PostMapping("/submit-test")
    public String submitTest(@RequestParam("testSlug") String testSlug, 
                             @RequestParam Map<String, String> allParams, 
                             Principal principal) {  
        if (principal == null) return "redirect:/login";

        TestResult result = testService.submitTest(testSlug, allParams, principal.getName());
        
        String courseSlug = result.getTest().getLesson().getCourse().getSlug();
        Long lessonId = result.getTest().getLesson().getId();

        return "redirect:/user/my-course-detail/" + courseSlug 
                + "?lessonId=" + lessonId 
                + "&testSlug=" + testSlug;
    }
}
