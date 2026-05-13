package com.payment.upimesh.model;

import java.math.BigDecimal;

// Change 'class' to 'record' here
public record PaymentEvent(String senderVpa, String receiverVpa, BigDecimal amount, String status) {

    @Override
    public String toString() {
        return String.format("Payment of ₹%.2f from %s to %s was %s",
                amount, senderVpa, receiverVpa, status);
    }
}