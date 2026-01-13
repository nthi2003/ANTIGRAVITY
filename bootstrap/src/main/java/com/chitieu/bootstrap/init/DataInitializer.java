package com.chitieu.bootstrap.init;

import com.chitieu.persistence.entity.TransactionEntity;
import com.chitieu.persistence.entity.UserEntity;
import com.chitieu.persistence.repository.AccountRepository;
import com.chitieu.persistence.repository.TransactionRepository;
import com.chitieu.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    @Override
    public void run(String... args) throws Exception {
        UUID demoId = UUID.fromString("00000000-0000-0000-0000-000000000001");

        if (!userRepository.existsByUsername("admin")) {
            userRepository.save(UserEntity.builder()
                    .username("admin")
                    .email("admin@chitieu.com")
                    .password("admin123")
                    .fullName("Administrator")
                    .build());
        }

        if (!userRepository.existsByUsername("demo_user")) {
            UserEntity user = UserEntity.builder()
                    .id(demoId)
                    .username("demo_user")
                    .email("demo@example.com")
                    .password("demo123")
                    .fullName("Hoang Anh")
                    .build();
            user = userRepository.save(user);

            // Create default bank account
            com.chitieu.persistence.entity.AccountEntity bankAccount = com.chitieu.persistence.entity.AccountEntity
                    .builder()
                    .name("Ngân hàng VCB")
                    .type(com.chitieu.domain.model.AccountType.BANK)
                    .balance(new BigDecimal("500000000"))
                    .currency("VND")
                    .user(user)
                    .build();
            bankAccount = accountRepository.save(bankAccount);

            // Add mock transactions linked to account
            transactionRepository.save(TransactionEntity.builder()
                    .user(user)
                    .account(bankAccount)
                    .amount(new BigDecimal("200000000"))
                    .category("Salary")
                    .type("INCOME")
                    .transactionDate(LocalDateTime.now())
                    .build());

            transactionRepository.save(TransactionEntity.builder()
                    .user(user)
                    .account(bankAccount)
                    .amount(new BigDecimal("75000000"))
                    .category("Rent")
                    .type("EXPENSE")
                    .transactionDate(LocalDateTime.now())
                    .build());

            System.out.println(">>> Demo data initialized for Neon Postgres!");
        }
    }
}
