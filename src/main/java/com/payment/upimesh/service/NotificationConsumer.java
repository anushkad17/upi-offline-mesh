package com.payment.upimesh.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.stereotype.Service;
import org.springframework.retry.annotation.Backoff;

@Service
public class NotificationConsumer {
    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    @Autowired
    private SmsService smsService;

    @Autowired
    private EmailService emailService;

    // Injecting values from .env via docker-compose
    @Value("${MY_MOBILE_NUMBER}")
    private String myMobileNumber;

    @Value("${MY_EMAIL_ADDRESS}")
    private String myEmailAddress;

    /**
     * Listens to payment-notifications topic.
     * Retries 3 times with an increasing delay if an exception occurs.
     */
    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 2000, multiplier = 2.0),
            dltStrategy = DltStrategy.FAIL_ON_ERROR
    )
    @KafkaListener(topics = "payment-notifications", groupId = "notification-group")
    public void onPaymentSettled(String message) {
        log.info("📩 Processing notification: {}", message);

        try {
            // 1. Send Localized SMS via Fast2SMS
            // Uses your real number from the .env file
            smsService.sendSms(myMobileNumber, "UPIMesh Alert: " + message);
            log.info("✅ Fast2SMS call triggered");

            // 2. Send Professional Email via Gmail
            // Uses your real email from the .env file
            emailService.sendEmail(myEmailAddress, message);
            log.info("✅ Gmail notification triggered");

        } catch (Exception e) {
            log.error("❌ Notification Pipeline Failed: {}", e.getMessage());
            // Rethrowing triggers the @RetryableTopic logic
            throw e;
        }
    }

    /**
     * This method is called if all 3 retry attempts fail.
     */
    @DltHandler
    public void handleDlt(String message) {
        log.error("💀 DEAD LETTER QUEUE: Notification permanently failed for: {}", message);
        // In a real app, you'd save this to a 'failed_notifications' table
    }
}