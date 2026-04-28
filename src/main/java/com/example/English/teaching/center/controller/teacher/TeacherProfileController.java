package com.example.English.teaching.center.controller.teacher;

import com.example.English.teaching.center.dto.report.TeacherProfileRequest;
import com.example.English.teaching.center.dto.user.PasswordChangeRequest;
import com.example.English.teaching.center.dto.user.UserProfileRequest;
import com.example.English.teaching.center.service.infra.ReCaptchaService;
import com.example.English.teaching.center.service.user.TeacherProfileService;
import com.example.English.teaching.center.service.user.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.beans.factory.annotation.Value;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/teacher/profile")
@PreAuthorize("hasRole('TEACHER')")
public class TeacherProfileController {
    private final TeacherProfileService teacherProfileService;
    private final UserService userService;
    private final ReCaptchaService reCaptchaService;

    @Value("${recaptcha.site-key}")
    private String recaptchaSiteKey;

    @GetMapping
    public String viewProfile(Model model, Principal principal,
            @RequestParam(value = "tab", defaultValue = "general") String tab ) {

        String email = principal.getName();
        model.addAttribute("user", userService.findByEmail(email));
        model.addAttribute("userProfileDTO", userService.getUserProfile(email));
        model.addAttribute("teacherProfile", teacherProfileService.getExpertiseProfile(email));
        model.addAttribute("passwordChangeDTO", new PasswordChangeRequest());
        model.addAttribute("pageTitle", "Thông tin cá nhân");

        model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
        model.addAttribute("activeTab", tab);

        return "teacher/profile-edit";
    }

    @PostMapping("/update-basic")
    public String updateBasicProfile(@ModelAttribute UserProfileRequest dto,
                @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
                @RequestParam(value = "g-recaptcha-response", required = false) String recaptchaResponse,
                Principal principal, RedirectAttributes ra) {
        try{
            boolean isHuman = reCaptchaService.verify(recaptchaResponse);
            if(!isHuman) 
                throw new RuntimeException("Mã xác thực Captcha không hợp lệ hoặc đã hết hạn!");

            userService.updateUserProfile(principal.getName(), dto, avatarFile);
            ra.addFlashAttribute("successMessage", "Cập nhật thông tin cá nhân thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/teacher/profile?tab=general";
    }

    @PostMapping("/update-expertise")
    public String updateExpertise(@ModelAttribute TeacherProfileRequest dto, 
                @RequestParam(value = "g-recaptcha-response", required = false) String recaptchaResponse,
                Principal principal, RedirectAttributes ra) {
        try{
            boolean isHuman = reCaptchaService.verify(recaptchaResponse);
            if (!isHuman) 
                throw new RuntimeException("Mã xác thực Captcha không hợp lệ hoặc đã hết hạn!");

            teacherProfileService.updateExpertiseProfile(principal.getName(), dto);
            ra.addFlashAttribute("successMessage", "Cập nhật thông tin chuyên môn thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/teacher/profile?tab=expertise";
    }

    @PostMapping("/change-password")
    public String changePassword(@ModelAttribute("passwordChangeDTO") PasswordChangeRequest dto,
                @RequestParam(value = "g-recaptcha-response", required = false) String recaptchaResponse,            
                Principal principal, RedirectAttributes ra) {
        try {
            boolean isHuman = reCaptchaService.verify(recaptchaResponse);
            if (!isHuman) 
                throw new RuntimeException("Mã xác thực Captcha không hợp lệ hoặc đã hết hạn!");

            userService.changePassword(principal.getName(), dto);
            ra.addFlashAttribute("successMessage", "Đổi mật khẩu thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/teacher/profile?tab=password";
    }
}