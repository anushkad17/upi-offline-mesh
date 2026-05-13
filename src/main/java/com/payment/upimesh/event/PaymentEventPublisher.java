package com.payment.upimesh.event;

import com.payment.upimesh.model.PaymentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Service
public class PaymentEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(PaymentEventPublisher.class);

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void publish(PaymentEvent event) {
        log.info("🚀 Publishing Kafka event: {}", event);
        // Topic: payment-notifications, Key: senderVpa, Value: event string
        kafkaTemplate.send("payment-notifications", event.senderVpa(), event.toString());
    }
    public void handleNotification(String message) {
        System.out.println("🔔 KAFKA NOTIFICATION CLOUD: " + message);
    }
}
