package com.example.English.teaching.center.service.finance;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.English.teaching.center.entity.Transaction;
import com.example.English.teaching.center.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookService {
    private final TransactionRepository transactionRepository;
    private final WalletService walletService;

    @Transactional(rollbackFor = Exception.class)
    public void processSePayWebhook(String txnRef, BigDecimal amountPaid){
        // 1. TÌM VÀ KHÓA TRANSACTION NGAY LẬP TỨC
        Transaction txn = transactionRepository.findByTxnRefForUpdate(txnRef)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mã giao dịch: " + txnRef));

        // 2. IDEMPOTENCY: Chỉ xử lý nếu đơn hàng đang PENDING
        // Nếu đã SUCCESS, FAILED, hoặc EXPIRED thì tuyệt đối không xử lý lại
        if(txn.getStatus() != Transaction.TransactionStatus.PENDING){
            log.info("Giao dịch {} đã ở trạng thái: {}. Bỏ qua webhook để tránh duplicate.", txnRef, txn.getStatus());
            return;
        }

        if(txn.getExpiredAt() != null && LocalDateTime.now().isAfter(txn.getExpiredAt())){
            log.error("⚠️ Tiền vào muộn! Giao dịch {} đã hết hạn từ {}. Yêu cầu xử lý thủ công.", txnRef, txn.getExpiredAt());
            // Cập nhật số tiền thực nhận nhưng KHÔNG cộng vào ví
            txn.setActualAmount(amountPaid);
            txn.setStatus(Transaction.TransactionStatus.FAILED);
            txn.setDescription("Giao dịch hết hạn nhưng nhận được tiền. Cần đối soát thủ công.");
            transactionRepository.save(txn);
            // Gợi ý: Bắn thông báo Alert cho Admin tại đây
            return;
        }

        log.info("⏳ Đang xử lý đối soát giao dịch {}. Số tiền nhận: {}", txnRef, amountPaid);

        // 3. Chuyển giao toàn bộ logic kiểm tra tiền và cộng ví cho WalletService
        walletService.confirmDeposit(txn, amountPaid);
    }
}