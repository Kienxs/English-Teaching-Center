package com.example.English.teaching.center.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import com.example.English.teaching.center.securty.ReCaptchaService;
import com.example.English.teaching.center.service.CourseCommentService;
import com.example.English.teaching.center.service.CourseService;
import com.example.English.teaching.center.service.TestService;
import com.example.English.teaching.center.service.UserService;
import com.example.English.teaching.center.dto.PasswordChangeDTO;
import com.example.English.teaching.center.dto.UserProfileDTO;
import com.example.English.teaching.center.entity.Course;
import com.example.English.teaching.center.entity.CourseComments;
import com.example.English.teaching.center.entity.StudentCourse;
import com.example.English.teaching.center.entity.TestResult;
import com.example.English.teaching.center.entity.User;
import com.example.English.teaching.center.exception.InvalidFileException;
import com.example.English.teaching.center.exception.RateLimitException;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
public class UserController {
    private final UserService userService;
    private final CourseCommentService courseCommentService;
    private final CourseService courseService;
    private final TestService testService;
    private final ReCaptchaService reCaptchaService;

    @Value("${recaptcha.site-key}")
    private String recaptchaSiteKey;

    @Autowired
    public UserController(UserService userService,
                          CourseCommentService courseCommentService,
                          CourseService courseService,
                          TestService testService,
                          ReCaptchaService reCaptchaService) {
        this.userService = userService;
        this.courseCommentService = courseCommentService;
        this.courseService = courseService;
        this.testService = testService;
        this.reCaptchaService = reCaptchaService;
    }

    // Landing page
    @GetMapping("/landing")
    public String showLandingPage() {
        return "landing";
    }

    // User Register
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user,
                           BindingResult bindingResult, 
                           Model model,
                           @RequestParam("g-recaptcha-response") String recaptchaResponse,
                           RedirectAttributes redirectAttributes) { 

        if(bindingResult.hasErrors()){
            FieldError passwordError = bindingResult.getFieldError("password");
            model.addAttribute("errorMessage", passwordError != null ?
                    passwordError.getDefaultMessage() : "Dữ liệu không hợp lệ");
            model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
            return "register";
        }

        try{
            userService.registerNewUser(user, recaptchaResponse);
            redirectAttributes.addFlashAttribute("successMessage", "Đăng ký thành công! Bạn có thể đăng nhập ngay.");
            return "redirect:/login";
        }catch(RateLimitException | IllegalArgumentException e){
            model.addAttribute("errorMessage", e.getMessage());
        }catch(Exception e){
            model.addAttribute("errorMessage", "Đã xảy ra lỗi trong quá trình đăng ký." + e.getMessage());
        }
        
        model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
        return "register";
    }

    // Spring Security sẽ xử lý POST /process-login
    @GetMapping("/login")
    public String showLoginForm( @RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            @RequestParam(value = "captcha", required = false) String captchaError,
                            Model model) {

        model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
        // 1. Xử lý thông báo lỗi đăng nhập (Email/Password sai)
        if (error != null) 
            model.addAttribute("errorMessage", "Email hoặc mật khẩu không chính xác!");

        // 2. Xử lý thông báo lỗi Captcha 
        if (captchaError != null) 
            model.addAttribute("captchaErrorMessage", "Vui lòng xác minh bạn không phải robot!");

        // 3. Xử lý thông báo đăng xuất
        if (logout != null) 
            model.addAttribute("successMessage", "Bạn đã đăng xuất thành công!");                  

        return "login";
    }    

    // User Dashboard------------------------------------------------------------
    @GetMapping("/user/home")
    public String userHome() {
        return "user/home";
    }

    @GetMapping("/user/courseList")
    public String courseList(){
        return "user/courseList";
    }

    @GetMapping("/user/teacher")
    public String teacher(){
        return "user/teacher";
    }

    @GetMapping("/user/my-course")
    public String learn(){
        return "user/my-course";
    }

    @GetMapping("/user/news")
    public String news(){
        return "user/news";
    }

    @GetMapping("/user/blog")
    public String blog(){
        return "user/blog";
    }

    @GetMapping("/user/advisory")
    public String advisory(){
        return "user/advisory";
    }

//------------------------- Process User Information--------------------------------
    @GetMapping("/user/userInfor")
    public String userInfor(Model model, Principal principal){
        String email = principal.getName();

        model.addAttribute("user", userService.findByEmail(email));
        model.addAttribute("userProfileDTO", userService.getUserProfile(email));
        model.addAttribute("passwordChangeDTO", new PasswordChangeDTO());
        return "user/userInfor";
    }

    @PostMapping("/user/userInfor/update")
    public String updateProfile(@ModelAttribute UserProfileDTO dto,
                                @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        try {
            userService.updateUserProfile(principal.getName(), dto, avatarFile);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thành công!");
        } catch (RateLimitException | InvalidFileException e) {
            // Bắt các lỗi nghiệp vụ từ Service và hiển thị ra View
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi hệ thống xảy ra!");
        }

        return "redirect:/user/userInfor";
    }

    @PostMapping("/user/userInfor/changePassword")
    public String changePassword(Principal principal,
                                 @ModelAttribute("passwordChangeDTO") PasswordChangeDTO dto,
                                 RedirectAttributes redirectAttributes) {
        try {
            userService.changePassword(principal.getName(), dto);
            redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đổi mật khẩu thất bại: " + e.getMessage());
        }
        return "redirect:/user/userInfor?tab=password";
    }

//--------------------------- Process download details for my course and courses-------------------------
    @GetMapping("/user/course-detail/{slug}")
    public String courseDetail(@PathVariable String slug, 
                            Model model, Principal principal,
                            HttpSession session){ 
        String email = (principal != null) ? principal.getName() : "anonymousUser";
        
        Map<String, Object> data = courseService.getCourseDetailData(slug, email, session);

        Course course = (Course) data.get("course");
        Page<CourseComments> pageComments = courseCommentService.getCommentsByCourseId(course.getId(), 0, 5);
        
        model.addAttribute("courseComments", pageComments.getContent());
        model.addAttribute("totalComments", pageComments.getTotalElements());
        model.addAllAttributes(data);
        return "user/course-detail";
    }


    @GetMapping("/user/my-courses") 
    public ResponseEntity<List<Course>> getMyEnrolledCourses(
        Principal principal,
        @RequestParam(name = "status", defaultValue = "ENROLLED") String statusString
    ) {
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

    @GetMapping("/user/my-course-detail/{courseSlug}")
    public String myCoursesDetail(@PathVariable String courseSlug, 
                                @RequestParam(name = "lessonId", required = false) Long lessonId,
                                @RequestParam(name = "testId", required = false) Long testId,
                                Model model, Principal principal) {

        if (principal == null) return "redirect:/login";

        User currentUser = userService.findByEmail(principal.getName());
        Map<String, Object> data = courseService.getMyCourseDetailData(courseSlug, lessonId, testId, currentUser.getId());

        model.addAllAttributes(data);
        return "user/my-course-detail"; 
    }

    @PostMapping("/user/course-detail/comment")
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

    // --- 1. HÀM BẮT ĐẦU LÀM BÀI  ---
    @GetMapping("/user/do-test/{testSlug}")
    public String doTest(@PathVariable String testSlug, 
                        Model model, 
                        Principal principal,
                        RedirectAttributes ra) {
        
        if (principal == null) return "redirect:/login";
        
        try {
            User user = userService.findByEmail(principal.getName());
            TestResult result = testService.startOrResumeTest(testSlug, user);

            model.addAttribute("test", result.getTest());
            model.addAttribute("resultId", result.getId());
            return "user/do-test";
        } catch (RuntimeException e) { 
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/user/home";
        }
    }

    // --- 2. HÀM NỘP BÀI  ---
    @PostMapping("/user/submit-test")
    public String submitTest(@RequestParam("testSlug") String testSlug, 
                             @RequestParam Map<String, String> allParams, 
                             Principal principal) {  
        if (principal == null) return "redirect:/login";

        TestResult result = testService.submitTest(testSlug, allParams, principal.getName());
        
        String courseSlug = result.getTest().getLesson().getCourse().getSlug();
        Long lessonId = result.getTest().getLesson().getId();

        return "redirect:/user/my-course-detail/" + courseSlug 
                + "?lessonId=" + lessonId 
                + "&testId=" + testSlug;
    }
}