package com.example.English.teaching.center.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Bắt riêng lỗi vượt quá dung lượng file
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSizeException(MaxUploadSizeExceededException exc, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", "Dung lượng file quá lớn! Vui lòng chọn ảnh dưới 2MB.");
        
        // Quay trở lại trang thông tin cá nhân kèm thông báo lỗi
        return "redirect:/user/userInfor?tab=profile";
    }
}