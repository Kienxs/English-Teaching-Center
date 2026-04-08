package com.example.English.teaching.center.controller.user;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.English.teaching.center.dto.user.PasswordChangeRequest;
import com.example.English.teaching.center.dto.user.UserProfileRequest;
import com.example.English.teaching.center.dto.user.UsernameChangeRequest;
import com.example.English.teaching.center.exception.InvalidFileException;
import com.example.English.teaching.center.exception.RateLimitException;
import com.example.English.teaching.center.service.infra.ReCaptchaService;
import com.example.English.teaching.center.service.user.UserService;

import jakarta.validation.Valid;

import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Value;

@Controller
@RequestMapping("/user/userInfor")
public class UserProfileController {
    private final UserService userService;
    private final ReCaptchaService reCaptchaService;

    @Value("${recaptcha.site-key}")
    private String recaptchaSiteKey;

    public UserProfileController(UserService userService, 
                                ReCaptchaService reCaptchaService) {
        this.userService = userService;
        this.reCaptchaService = reCaptchaService;
    }

    @GetMapping
    public String userInfor(Model model, Principal principal){
        String email = principal.getName();

        model.addAttribute("user", userService.findByEmail(email));
        model.addAttribute("userProfileDTO", userService.getUserProfile(email));
        model.addAttribute("passwordChangeDTO", new PasswordChangeRequest());
        model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);

        return "user/userInfor";
    }

    @PostMapping("/update")
    public String updateProfile(@ModelAttribute UserProfileRequest dto,
                                @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
                                @RequestParam(value = "g-recaptcha-response", required = false) String recaptchaResponse,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        try {
            boolean isHuman = reCaptchaService.verify(recaptchaResponse);

            if(!isHuman)
                throw new RuntimeException("Mã xác thực Captcha không hợp lệ hoặc đã hết hạn!");

            userService.updateUserProfile(principal.getName(), dto, avatarFile);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thành công!");
        } catch (RateLimitException | InvalidFileException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cập nhật thất bại: " + e.getMessage());
        }

        return "redirect:/user/userInfor";
    }

    @PostMapping("/changePassword")
    public String changePassword(Principal principal,
                        @ModelAttribute("passwordChangeDTO") PasswordChangeRequest dto,
                        @RequestParam(value="g-recaptcha-response", required = false) String recaptchaResponse,
                        RedirectAttributes redirectAttributes) {
        try {
            reCaptchaService.verify(recaptchaResponse);

            userService.changePassword(principal.getName(), dto);
            redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đổi mật khẩu thất bại: " + e.getMessage());
        }
        return "redirect:/user/userInfor?tab=password";
    }

    @PostMapping("/changeUsername")
    public String changeUsername(Principal principal,
                    @Valid @ModelAttribute("usernameChangeDTO") UsernameChangeRequest dto,
                    @RequestParam(value="g-recaptcha-response", required = false) String recaptchaResponse,
                    RedirectAttributes redirectAttributes){

        try{
            reCaptchaService.verify(recaptchaResponse);
            
            userService.changeUsername(principal.getName(), dto);
            redirectAttributes.addFlashAttribute("successMessage", "Đổi tên tài khoản thành công");
        }catch(Exception e){
            redirectAttributes.addFlashAttribute("errorMessage", "Đổi tên thấy bại: " + e.getMessage());
        }

        return "redirect:/user/userInfor?tab=username";
    }
}
