package com.example.English.teaching.center.service.finance;

import java.math.BigDecimal;
import java.util.UUID;

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
public class WalletService {
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    @Transactional(rollbackFor = Exception.class)
    public void confirmDeposit(Transaction txn, BigDecimal amountPaid){
        BigDecimal expectedAmount = txn.getAmount();

        // 1. Kiểm tra nạp thiếu -> Đánh dấu FAILED và dừng luồng
        if(amountPaid.compareTo(expectedAmount) < 0){
            log.warn("🚨 Khách chuyển thiếu tiền. Đơn: {}, Yêu cầu: {}, Thực nhận: {}", 
                     txn.getTxnRef(), expectedAmount, amountPaid);
            txn.setStatus(Transaction.TransactionStatus.FAILED);
            txn.setActualAmount(amountPaid);
            txn.setDescription(String.format("Thất bại: Chuyển thiếu tiền. Cần %s, nhận %s", expectedAmount, amountPaid));
            transactionRepository.save(txn);
            return;
        }

        // 2. Khóa User để cập nhật số dư an toàn (Pessimistic Locking)
        User user = userRepository.findByEmailForUpdate(txn.getUser().getEmail())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng trên hệ thống!"));

        BigDecimal currentBalance = user.getBalance() == null ? BigDecimal.ZERO : user.getBalance();

        //3. Cộng số tiền đã yêu cầu
        BigDecimal newBalance = currentBalance.add(expectedAmount);
        user.setBalance(newBalance);
        userRepository.save(user);

        // 4. Xử lý đối soát phần tiền thừa
        if(amountPaid.compareTo(expectedAmount) > 0){
            BigDecimal excessAmount = amountPaid.subtract(expectedAmount);
            log.warn("⚠️ Khách chuyển dư tiền. Mã đơn: {}, Dư: {} VNĐ", txn.getTxnRef(), excessAmount);
            txn.setDescription(String.format("Thành công (Cộng đúng %s). Phần dư %s VNĐ đang chờ xử lý.", expectedAmount, excessAmount));
        }else{
            txn.setDescription("Nạp tiền thành công.");
        }

        // 5. Cập nhật Transaction
        txn.setActualAmount(amountPaid);
        txn.setBalanceAfter(newBalance);
        txn.setStatus(Transaction.TransactionStatus.SUCCESS);
        transactionRepository.save(txn);

        log.info("✅ Cập nhật ví thành công cho đơn {}. Ví: {} -> {}", txn.getTxnRef(), currentBalance, newBalance);
    }

    @Transactional(rollbackFor = Exception.class)
    public void payment(String email, BigDecimal amount, String description) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Số tiền thanh toán phải lớn hơn 0!");

        User user = userRepository.findByEmailForUpdate(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));

        // 1. Check balance
        BigDecimal currentBalance = user.getBalance() == null ? BigDecimal.ZERO : user.getBalance();
        if (currentBalance.compareTo(amount) < 0)
            throw new RuntimeException("Số dư không đủ để thực hiện giao dịch!");

        // 2. Deduct balance
        BigDecimal newBalance = currentBalance.subtract(amount);
        user.setBalance(newBalance);
        userRepository.save(user);

        // 3. Record transaction log
        Transaction tx = new Transaction();
        tx.setUser(user);
        tx.setTxnRef("PAY-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 6)); // Bổ sung mã TxnRef
        tx.setAmount(amount.negate()); 
        tx.setActualAmount(amount.negate());
        tx.setBalanceAfter(newBalance);
        tx.setType(Transaction.TransactionType.PAYMENT);
        tx.setStatus(Transaction.TransactionStatus.SUCCESS);
        tx.setDescription(description);
        transactionRepository.save(tx);
    }
}