package com.example.English.teaching.center.controller.auth;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;

import com.example.English.teaching.center.dto.ResetPasswordDTO;
import com.example.English.teaching.center.dto.UserRegisterDTO;
import com.example.English.teaching.center.entity.User;
import com.example.English.teaching.center.exception.RateLimitException;
import com.example.English.teaching.center.service.auth.AuthService;
import com.example.English.teaching.center.service.infra.ReCaptchaService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;

@Controller
public class AuthController {
    private final AuthService authService;
    private final ReCaptchaService reCaptchaService;

    @Value("${recaptcha.site-key}")
    private String recaptchaSiteKey;

    public AuthController(AuthService authService, 
                        ReCaptchaService reCaptchaService) {
        this.authService = authService;
        this.reCaptchaService = reCaptchaService;
    }

    @GetMapping({"/", "/landing"})
    public String showLandingPage(Authentication authentication) {
        if(authentication != null && authentication.isAuthenticated()){
            return redirectUserBaseOnRole(authentication);
        }

        return "landing";
    }
// Register --------------------------------------------------
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UserRegisterDTO dto,
                           BindingResult bindingResult, 
                           Model model,
                           @RequestParam("g-recaptcha-response") String recaptchaResponse,
                           RedirectAttributes redirectAttributes,
                           Authentication authentication) { 

        if(authentication != null && authentication.isAuthenticated()){
            return redirectUserBaseOnRole(authentication);
        }

        if(bindingResult.hasErrors()){
            FieldError passwordError = bindingResult.getFieldError("password");
            model.addAttribute("errorMessage", passwordError != null ?
                    passwordError.getDefaultMessage() : "Dữ liệu không hợp lệ");
            model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
            return "register";
        }

        try{
            authService.registerNewUser(dto, recaptchaResponse);
            redirectAttributes.addFlashAttribute("successMessage", "Đăng ký thành công! Chúng tôi đã gửi một link kích hoạt đến email của bạn. Vui lòng kiểm tra hộp thư (kể cả mục Thư rác/Spam) nhé!");
            return "redirect:/login";
        }catch(RateLimitException | IllegalArgumentException e){
            model.addAttribute("errorMessage", e.getMessage());
        }catch(Exception e){
            model.addAttribute("errorMessage", "Đã xảy ra lỗi trong quá trình đăng ký." + e.getMessage());
        }
        
        model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
        return "register";
    }

    @GetMapping("/verify")
    public String verifyAccount(@RequestParam("code") String code, RedirectAttributes redirectAttributes) {
        boolean isVerified = authService.verifyEmail(code);
        
        if (isVerified) {
            redirectAttributes.addFlashAttribute("successMessage", "Kích hoạt tài khoản thành công! Chào mừng bạn gia nhập ECE, vui lòng đăng nhập.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Đường dẫn kích hoạt không hợp lệ, hoặc tài khoản đã được kích hoạt từ trước.");
        }
        
        return "redirect:/login"; 
    }

// Login -------------------------------------------
    @GetMapping("/login")
    public String showLoginForm( @RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            @RequestParam(value = "captcha", required = false) String captchaError,
                            @RequestParam(value = "kicked", required = false) String kicked,
                            HttpSession session,
                            Model model,
                            Authentication authentication) {

        if(authentication != null && authentication.isAuthenticated()){
            return redirectUserBaseOnRole(authentication);
        }

        model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
        // 1. Xử lý thông báo lỗi đăng nhập (Email/Password sai)
        if (error != null) {
            // Lấy thông báo lỗi cuối cùng từ Spring Security Session
            Exception lastException = (Exception) session.getAttribute("SPRING_SECURITY_LAST_EXCEPTION");
            String errorMsg = (lastException != null) ? lastException.getMessage() : "Email hoặc mật khẩu không đúng!";
            model.addAttribute("errorMessage", errorMsg);
        }
        
        if (kicked != null) {
            model.addAttribute("kickedMessage", "Tài khoản đã được đăng nhập ở nơi khác hoặc phiên làm việc hết hạn.");
        }

        // 2. Xử lý thông báo lỗi Captcha 
        if (captchaError != null) 
            model.addAttribute("captchaErrorMessage", "Vui lòng xác minh bạn không phải robot!");

        // 3. Xử lý thông báo đăng xuất
        if (logout != null) 
            model.addAttribute("successMessage", "Bạn đã đăng xuất thành công!");                  

        return "login";
    }    

    private String redirectUserBaseOnRole(Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")
                    || auth.getAuthority().equals("ROLE_TECHNICAL"));

        boolean isTeacher = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_TEACHER"));

        if(isAdmin){
            return "redirect:/admin/dashboard";
        } else if(isTeacher){
            return "redirect:/teacher/course-management";
        } else {
            return "redirect:/user/home";
        }
    }

// Forgot Password ----------------------------------------------
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm(Model model){
        model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email,
                @RequestParam(name = "g-recaptcha-response", required = false) String recaptchaResponse,                
                RedirectAttributes ra){
        
        if(!reCaptchaService.verify(recaptchaResponse)){
            ra.addFlashAttribute("errorMessage", "Vui lòng xác nhận bạn không phải robot!");
            return "redirect:/forgot-password";
        }

        try {
            authService.generatePasswordResetToken(email);
            ra.addFlashAttribute("successMessage", "Chúng tôi đã gửi đường link đặt lại mật khẩu vào email của bạn. Vui lòng kiểm tra!"); 
        } catch (RateLimitException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            ra.addFlashAttribute("successMessage", "Chúng tôi đã gửi đường link đặt lại mật khẩu vào email của bạn. Vui lòng kiểm tra!");
        }

        return "redirect:/forgot-password";
    }

// Reset Password ----------------------------------------
    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam(value = "token") String token, 
                                        Model model, 
                                        RedirectAttributes ra) {
        User user = authService.getByResetPasswordToken(token);
        if (user == null) {
            ra.addFlashAttribute("errorMessage", "Đường dẫn không hợp lệ hoặc đã hết hạn!");
            return "redirect:/login";
        }
        model.addAttribute("token", token);
        return "reset-password"; 
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@Valid @ModelAttribute ResetPasswordDTO dto,
                                       BindingResult bindingResult,
                                       RedirectAttributes ra) {
                                    
        if(bindingResult.hasErrors()){
            ra.addFlashAttribute("errorMessage", bindingResult.getFieldError().getDefaultMessage());
            return "redirect:/reset-password?token=" + dto.getToken();
        }             

        User user = authService.getByResetPasswordToken(dto.getToken());
        if (user == null) {
            ra.addFlashAttribute("errorMessage", "Đường dẫn không hợp lệ hoặc đã hết hạn!");
            return "redirect:/login";
        }

        authService.updatePasswordByToken(user, dto.getPassword());
        ra.addFlashAttribute("successMessage", "Bạn đã đặt lại mật khẩu thành công. Vui lòng đăng nhập!");
        return "redirect:/login";
    }
}
