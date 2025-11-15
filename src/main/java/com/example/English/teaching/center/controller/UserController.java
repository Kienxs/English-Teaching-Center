package com.example.English.teaching.center.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import com.example.English.teaching.center.model.User;
import com.example.English.teaching.center.model.dto.UserProfileDTO;
import com.example.English.teaching.center.repository.UserRepository;
import com.example.English.teaching.center.securty.ReCaptchaService;
import com.example.English.teaching.center.service.CourseService;
import com.example.English.teaching.center.service.UserService;
import com.example.English.teaching.center.model.Course;
import com.example.English.teaching.center.model.dto.PasswordChangeDTO;

import jakarta.validation.Valid;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;


@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Value("${recaptcha.site-key}")
    private String recaptchaSiteKey;

    private final ReCaptchaService reCaptchaService;
    private final CourseService courseService;

    @Autowired
    public UserController(UserRepository userRepository,
                         UserService userService,
                         ReCaptchaService reCaptchaService,
                         CourseService courseService) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.reCaptchaService = reCaptchaService;
        this.courseService = courseService;
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
                           @RequestParam("g-recaptcha-response") String recaptchaResponse) { 

    // 1. Xác thực reCAPTCHA trước
    if (!reCaptchaService.verify(recaptchaResponse)) {
        model.addAttribute("errorMessage", "Xác thực CAPTCHA không thành công. Vui lòng thử lại.");
        model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
        return "register";
    }

        // 2. Kiểm tra lỗi validation của form (ví dụ: mật khẩu yếu)
        if(bindingResult.hasErrors()){
            FieldError passwordError = bindingResult.getFieldError("password");
            if(passwordError != null){
                model.addAttribute("errorMessage", passwordError.getDefaultMessage());
            } else {
                model.addAttribute("errorMessage", "Dữ liệu không hợp lệ, vui lòng kiểm tra lại.");
            }
            model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
            return "register";
        }

        // 3. Nếu mọi thứ hợp lệ, tiến hành đăng ký
        try {
            userService.register(user);
            model.addAttribute("successMessage", "Đăng ký thành công! Bạn có thể đăng nhập ngay.");
            model.addAttribute("user", new User()); // Reset form
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Đã xảy ra lỗi trong quá trình đăng ký.");
        }
        
        model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
        return "register";
    }

    // Spring Security sẽ xử lý POST /process-login
    @GetMapping("/login")
    public String showLoginForm( @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "message", required = false) String message,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "captcha", required = false) String captchaError,
            Model model) {

        model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
        model.addAttribute("loginError", error != null);
        model.addAttribute("captchaError", captchaError != null);
        model.addAttribute("errorMessage", message != null ? message : (error != null ? "Email hoặc mật khẩu không đúng!" : null));
        model.addAttribute("captchaErrorMessage", captchaError != null ? "Vui lòng xác minh bạn không phải robot!" : null);
        model.addAttribute("logoutMessage", logout != null ? "Bạn đã đăng xuất thành công!" : null);

        return "login";
    }


    // Admin Dashboard-----------------------------------------------------------
    @GetMapping("/admin/dashboard")
    public String dashboard() {
        return "admin/dashboard"; 
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

    @GetMapping("/user/course/{id}")
        public String getCourseDetail(@PathVariable("id") Long id, Model model) {
            
            try {
            // 1. Gọi hàm trả về Optional
            Optional<Course> courseOptional = courseService.findCourseById(id);

            // 2. Lấy đối tượng Course, nếu không có sẽ ném lỗi
            Course course = courseOptional.orElseThrow(() -> new NoSuchElementException("Không tìm thấy khóa học với ID: " + id));
            
            // 3. Đưa vào model
            model.addAttribute("course", course);
            
            // 4. Trả về tên template
            return "user/course-detail"; // (Tên file: /templates/user/course-detail.html)

        } catch (NoSuchElementException e) {
            // 5. Nếu không tìm thấy, quay về trang danh sách
            return "redirect:/user/courseList?error=notfound";
        } catch (Exception e) {
            // 6. Lỗi chung khác
            return "redirect:/user/courseList?error=generic";
        }
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

    // Process User Information
    @GetMapping("/user/userInfor")
    public String userInfor(Model model, Principal principal){
        String email = principal.getName();
        User user = userService.findByEmail(email);
        model.addAttribute("user", user);

        if(!model.containsAttribute("userProfileDTO")){
            // Điền sẵn DTO với thông tin của user
            UserProfileDTO dto = new UserProfileDTO();
            dto.setFullName(user.getName()); // Giả sử DTO có setFullName
            dto.setPhone(user.getPhone());   // Giả sử DTO có setPhone

            model.addAttribute("userProfileDTO", dto);
        }
        if(!model.containsAttribute("passwordChangeDTO")){
            model.addAttribute("passwordChangeDTO", new PasswordChangeDTO());
        }

        return "user/userInfor";
    }

    @PostMapping("/user/userInfor/update")
    public String updateProfile(Principal principal,
                                @Valid @ModelAttribute("userProfileDTO") UserProfileDTO dto,
                                @RequestParam("avatarFile") MultipartFile avatarFile,
                                RedirectAttributes redirectAttributes){
        if (principal == null) {
            return "redirect:/login";
        }


        try {
            userService.updateUserProfile(principal.getName(), dto);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cập nhật thất bại: " /*+ e.getMessage()*/);
        }
        return "redirect:/user/userInfor";
    }

    @PostMapping("/user/userInfor/changePassword")
    public String changePassword(Principal principal,
                                 @ModelAttribute("passwordChangeDTO") PasswordChangeDTO dto,
                                 RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }
        try {
            userService.changePassword(principal.getName(), dto);
            redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đổi mật khẩu thất bại: " /*+ e.getMessage()*/);
        }
        // Chuyển hướng người dùng quay lại tab mật khẩu
        return "redirect:/user/userInfor?tab=password";
    }
}