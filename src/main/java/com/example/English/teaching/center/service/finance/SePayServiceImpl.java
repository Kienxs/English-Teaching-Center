package com.example.English.teaching.center.service.finance;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.English.teaching.center.entity.Transaction;
import com.example.English.teaching.center.entity.User;
import com.example.English.teaching.center.repository.TransactionRepository;
import com.example.English.teaching.center.repository.UserRepository;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.net.URLEncoder;

@Service("SEPAY")
@RequiredArgsConstructor
@Slf4j
public class SePayServiceImpl implements PaymentStrategy {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${sepay.transfer-prefix}")
    private String transferPrefix;

    @Value("${sepay.bank-id}")
    private String bankId;

    @Value("${sepay.account-no}")
    private String accountNo;

    @Value("${sepay.account-name}")
    private String accountName;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createPaymentUrl(String email, BigDecimal amount) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng"));

        // 1. Tạo "Nội dung chuyển khoản unique"
        String randomSuffix = String.format("%06d", secureRandom.nextInt(999999));
        String orderCode = transferPrefix + (System.currentTimeMillis() % 10000L) + randomSuffix;

        // 2. Lưu giao dịch trạng thái PENDING
        Transaction txn = new Transaction();
        txn.setUser(user);
        txn.setTxnRef(orderCode); 
        txn.setAmount(amount);
        txn.setActualAmount(BigDecimal.ZERO);
        txn.setType(Transaction.TransactionType.DEPOSIT);
        txn.setStatus(Transaction.TransactionStatus.PENDING);
        txn.setPaymentMethod("VIETQR");
        txn.setDescription("Nạp tiền hệ thống: " + orderCode);
        transactionRepository.save(txn);

        // 3. Tạo QR Động
        try{
            String encodedName = URLEncoder.encode(accountName, StandardCharsets.UTF_8.toString()).replace("+", "%20");
            
            String qrImageUrl = String.format("https://img.vietqr.io/image/%s-%s-compact2.jpg?amount=%d&addInfo=%s&accountName=%s",
                    bankId, accountNo, amount.intValue(), orderCode, encodedName);
            log.info("Tạo QR thành công cho mã đơn: {}", orderCode);
            return qrImageUrl;
        } catch (Exception e) {
            log.error("Lỗi encode tạo mã QR", e);
            throw new RuntimeException("Lỗi hệ thống khi tạo QR.");
        }
    }
}