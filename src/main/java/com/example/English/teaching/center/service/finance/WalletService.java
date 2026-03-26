package com.example.English.teaching.center.service.finance;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.example.English.teaching.center.entity.Transaction;
import com.example.English.teaching.center.entity.User;
import com.example.English.teaching.center.repository.TransactionRepository;
import com.example.English.teaching.center.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class WalletService {
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public WalletService(UserRepository userRepository, TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional 
    public void deposit(String email, BigDecimal amount){
        // 1. Validate amount minimum 
        if(amount == null || amount.compareTo(new BigDecimal("10000")) < 0){
            throw new IllegalArgumentException("Số tiền nạp tối thiểu là 10.000đ!");
        }

        // 2. Find user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng trên hệ thống!"));

        // 3. Calculate the new balance
        BigDecimal currentBalance = user.getBalance() == null ? BigDecimal.ZERO : user.getBalance();
        BigDecimal newBalance = currentBalance.add(amount);

        // 4. Update balance
        user.setBalance(newBalance);
        userRepository.save(user); // Thực ra có @Transactional thì dòng này có thể bỏ, Hibernate tự update (Dirty Checking)

        // 5. Record transaction log
        Transaction tx = new Transaction();
        tx.setUser(user);
        tx.setAmount(amount);
        tx.setBalanceAfter(newBalance);
        tx.setType(Transaction.TransactionType.DEPOSIT);
        tx.setDescription("Nạp tiền vào ví qua hệ thống!");
        transactionRepository.save(tx);
    }

    @Transactional
    public void payment(User user, BigDecimal amount, String description){
        if(amount == null || amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("Số tiền thanh toán phải lớn hơn 0!");
        }

        // 1. Check balance
        BigDecimal currentBalance = user.getBalance() == null ? BigDecimal.ZERO : user.getBalance();
        if(currentBalance.compareTo(amount) < 0){
            throw new RuntimeException("Số dư không đủ để thực hiện giao dịch!");
        }

        // 2. Deduct balance
        BigDecimal newBalance = currentBalance.subtract(amount);
        user.setBalance(newBalance);

        // 3. Record transaction log
        Transaction tx = new Transaction();
        tx.setUser(user);
        tx.setAmount(amount.negate()); // Giao dịch thanh toán nên là số âm
        tx.setBalanceAfter(newBalance);
        tx.setType(Transaction.TransactionType.PAYMENT);
        tx.setDescription(description);
        transactionRepository.save(tx);
    }
}