package com.example.English.teaching.center.controller.payment;
import org.springframework.stereotype.Controller; 
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.English.teaching.center.service.finance.WalletService;

import java.math.BigDecimal;
import java.security.Principal;

@Controller
@RequestMapping("/user") 
public class WalletController { 
    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping("/deposit") 
    public String depositMoney(@RequestParam("amount") BigDecimal amount, 
                            RedirectAttributes redirectAttributes,
                            Principal principal) {
        if(principal == null) return "redirect:/login";
        
        try{
            walletService.deposit(principal.getName(), amount);

            redirectAttributes.addFlashAttribute("successMessage", 
                "Nạp thành công " + String.format("%,.0f", amount) + "đ!");
        }catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi giao dịch. Vui lòng thử lại sau.");
        }

        return "redirect:/user/userInfor?tab=wallet";
    }
}