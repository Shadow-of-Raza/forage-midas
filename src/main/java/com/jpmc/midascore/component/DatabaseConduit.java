package com.jpmc.midascore.component;

import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.repository.UserRepository;
import com.jpmc.midascore.repository.TransactionRepository;
import org.springframework.stereotype.Component;

@Component
public class DatabaseConduit {
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public DatabaseConduit(UserRepository userRepository, TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    public UserRecord findById(long id) {
        return userRepository.findById(id);
    }

    public void saveTransaction(TransactionRecord record) {
        transactionRepository.save(record);
    }

    public void saveUser(UserRecord user) {
        userRepository.save(user);
    }
}