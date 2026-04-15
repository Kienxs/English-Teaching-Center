package com.example.English.teaching.center.controller.payment;

import com.example.English.teaching.center.entity.User;
import com.example.English.teaching.center.repository.UserRepository;
import com.example.English.teaching.center.service.finance.PaymentStrategy;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;

@Controller
@RequestMapping("/user") 
public class WalletController { 
    private final PaymentStrategy paymentStrategy;

    private final UserRepository userRepository;

    public WalletController(@Qualifier("SEPAY") PaymentStrategy paymentStrategy,
                            UserRepository userRepository) {
        this.paymentStrategy = paymentStrategy;
        this.userRepository = userRepository;
    }

    @PostMapping("/deposit") 
    public String depositMoney(@RequestParam("amount") BigDecimal amount, 
                            RedirectAttributes redirectAttributes,
                            Principal principal) {
        try{
            String qrImageUrl = paymentStrategy.createPaymentUrl(principal.getName(), amount);

            redirectAttributes.addFlashAttribute("qrImageUrl", qrImageUrl);
            redirectAttributes.addFlashAttribute("depositAmount", amount);
        }catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể tạo mã giao dịch lúc này.");
        }

        return "redirect:/user/userInfor?tab=wallet";
    }


    @GetMapping("/api/balance")
    @ResponseBody
    public ResponseEntity<BigDecimal> getCurrentBalance(Principal principal){
        if(principal == null) return ResponseEntity.status(401).build();

        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
        
        BigDecimal balance = user.getBalance() != null ? user.getBalance() : BigDecimal.ZERO;

        return ResponseEntity.ok(balance);
    }
}