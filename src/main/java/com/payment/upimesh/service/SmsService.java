package com.payment.upimesh.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class SmsService {

    @Value("${FAST2SMS_API_KEY}")
    private String apiKey;

    public void sendSms(String phoneNumber, String message) {
        try {
            // Fast2SMS Quick SMS Route (Bulk V2)
            String url = "https://www.fast2sms.com/dev/bulkV2?authorization=" + apiKey +
                    "&route=q&message=" + URLEncoder.encode(message, StandardCharsets.UTF_8) +
                    "&flash=0&numbers=" + phoneNumber;

            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(url, String.class);

            System.out.println("📲 Fast2SMS Response: " + response);
        } catch (Exception e) {
            System.err.println("❌ Fast2SMS Request Failed: " + e.getMessage());
        }
    }
}