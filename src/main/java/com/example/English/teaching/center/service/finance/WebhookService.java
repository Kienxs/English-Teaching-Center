package com.example.English.teaching.center.service.finance;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.English.teaching.center.entity.Transaction;
import com.example.English.teaching.center.entity.User;
import com.example.English.teaching.center.repository.TransactionRepository;
import com.example.English.teaching.center.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookService {
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Transactional(rollbackFor = Exception.class)
    public void processSePayWebhook(String txnRef, BigDecimal amountPaid){
        // 1. TÌM VÀ KHÓA TRANSACTION NGAY LẬP TỨC
        Transaction txn = transactionRepository.findByTxnRefForUpdate(txnRef)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mã giao dịch: " + txnRef));

        // 2. IDEMPOTENCY: Kiểm tra trạng thái an toàn
        if(txn.getStatus() == Transaction.TransactionStatus.SUCCESS ||
                txn.getStatus() == Transaction.TransactionStatus.FAILED){
            log.info("Giao dịch {} đã được xử lý xong từ trước. Trạng thái hiện tại: {}", txnRef, txn.getStatus());
            return;
        }

        BigDecimal expectedAmount = txn.getAmount();

        // 3. ANTI-FRAUD: Kiểm tra khách nạp thiếu tiền
        if(amountPaid.compareTo(expectedAmount) < 0){
            log.warn("🚨 CẢNH BÁO GIAN LẬN/NẠP THIẾU: Đơn {} yêu cầu {}đ nhưng chỉ nhận {}đ", 
                    txnRef, expectedAmount, amountPaid);

            txn.setStatus(Transaction.TransactionStatus.FAILED);
            txn.setDescription(String.format("Thất bại: Chuyển thiếu tiền. Yêu cầu %s, thực nhận %s", expectedAmount, amountPaid));
            transactionRepository.save(txn);

            return;
        }

        // 4. Khóa User để cập nhật số dư an toàn
        User user = userRepository.findByEmailForUpdate(txn.getUser().getEmail())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));
                
        // 5. Cộng tiền
        BigDecimal currentBalance = user.getBalance() == null ? BigDecimal.ZERO : user.getBalance();
        BigDecimal newBalance = currentBalance.add(amountPaid);
        user.setBalance(newBalance);
        userRepository.save(user);

        // 6. Cập nhật trạng thái thành công
        txn.setBalanceAfter(newBalance);
        txn.setStatus(Transaction.TransactionStatus.SUCCESS);
        if (amountPaid.compareTo(expectedAmount) > 0) 
             txn.setDescription("Nạp tiền thành công (Khách chuyển dư). Thực nhận: " + amountPaid);
        else 
             txn.setDescription("Nạp tiền thành công.");

        transactionRepository.save(txn);

        log.info("✅ Xử lý thành công đơn {}. Thực nhận: {}. Số dư mới: {}", txnRef, amountPaid, newBalance);
    }
}