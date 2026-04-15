package com.example.English.teaching.center.service.finance;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${wallet.deposit.min-amount:10000}")
    private BigDecimal minDepositAmount;

    @Transactional(rollbackFor = Exception.class)
    public void deposit(String email, BigDecimal amount){
        // 1. Validate amount minimum 
        if(amount == null || amount.compareTo(minDepositAmount) < 0)
            throw new IllegalArgumentException("Số tiền nạp tối thiểu là " + minDepositAmount);

        // 2. Find user by email
        User user = userRepository.findByEmailForUpdate(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng trên hệ thống!"));

        // 3. Calculate the new balance
        BigDecimal currentBalance = user.getBalance() == null ? BigDecimal.ZERO : user.getBalance();
        BigDecimal newBalance = currentBalance.add(amount);

        // 4. Update balance
        user.setBalance(newBalance);

        // 5. Record transaction log
        Transaction tx = new Transaction();
        tx.setUser(user);
        tx.setAmount(amount);
        tx.setBalanceAfter(newBalance);
        tx.setType(Transaction.TransactionType.DEPOSIT);
        tx.setStatus(Transaction.TransactionStatus.SUCCESS); 
        tx.setDescription("Nạp tiền vào ví qua hệ thống!");
        transactionRepository.save(tx);
    }

    @Transactional(rollbackFor = Exception.class)
    public void payment(String email, BigDecimal amount, String description){
        if(amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Số tiền thanh toán phải lớn hơn 0!");

        User user = userRepository.findByEmailForUpdate(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));

        // 1. Check balance
        BigDecimal currentBalance = user.getBalance() == null ? BigDecimal.ZERO : user.getBalance();
        if(currentBalance.compareTo(amount) < 0)
            throw new RuntimeException("Số dư không đủ để thực hiện giao dịch!");

        // 2. Deduct balance
        BigDecimal newBalance = currentBalance.subtract(amount);
        user.setBalance(newBalance);

        // 3. Record transaction log
        Transaction tx = new Transaction();
        tx.setUser(user);
        tx.setAmount(amount.negate()); 
        tx.setBalanceAfter(newBalance);
        tx.setType(Transaction.TransactionType.PAYMENT);
        tx.setStatus(Transaction.TransactionStatus.SUCCESS);
        tx.setDescription(description);
        transactionRepository.save(tx);
    }
}