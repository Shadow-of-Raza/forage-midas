package com.jpmc.midascore.component;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Incentive;
import com.jpmc.midascore.foundation.Transaction;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class KafkaConsumer {
    private final DatabaseConduit databaseConduit;
    private final RestTemplate restTemplate;
    private final String INCENTIVE_API_URL = "http://localhost:8080/incentive";

    public KafkaConsumer(DatabaseConduit databaseConduit) {
        this.databaseConduit = databaseConduit;
        this.restTemplate = new RestTemplate();
    }

    @KafkaListener(topics = "${general.kafka-topic}")
    public void listen(Transaction transaction) {
        UserRecord sender = databaseConduit.findById(transaction.getSenderId());
        UserRecord recipient = databaseConduit.findById(transaction.getRecipientId());

        if (sender != null && recipient != null && sender.getBalance() >= transaction.getAmount()) {

            // 1. Call External Incentive API (The Black Box)
            Incentive response = restTemplate.postForObject(INCENTIVE_API_URL, transaction, Incentive.class);
            float incentiveAmount = (response != null) ? response.getAmount() : 0f;

            // 2. Update balances (Note: Incentive is ADDED to recipient, but NOT deducted from sender)
            sender.setBalance(sender.getBalance() - transaction.getAmount());
            recipient.setBalance(recipient.getBalance() + transaction.getAmount() + incentiveAmount);

            // 3. Save to Database
            databaseConduit.saveUser(sender);
            databaseConduit.saveUser(recipient);

            TransactionRecord record = new TransactionRecord(
                    sender,
                    recipient,
                    transaction.getAmount(),
                    incentiveAmount
            );
            databaseConduit.saveTransaction(record);
        }
    }
}