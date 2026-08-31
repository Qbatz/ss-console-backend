package com.smartstay.console.services;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.smartstay.console.dao.CustomerCredentials;
import com.smartstay.console.dao.Customers;
import com.smartstay.console.dao.KycDetails;
import com.smartstay.console.ennum.NotificationMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class FcmNotificationService {

    @Autowired
    private FirebaseMessaging firebaseMessaging;
    @Autowired
    private CustomersCredentialService customersCredentialService;

    public void sendKycReminderNotification(Customers customer, KycDetails kycDetails) {

        if (customer != null && kycDetails != null) {

            CustomerCredentials customerCredential = customersCredentialService
                    .findByXuid(customer.getXuid());

            if (customerCredential != null && customerCredential.getFcmToken() != null) {

                HashMap<String, String> payload = new HashMap<>();
                payload.put("title", "KYC request");
                payload.put("type", NotificationMessage.KYC_REQUESTS.name());
                payload.put("request_id", kycDetails.getEntityId());
                payload.put("token_id", kycDetails.getAccessTokenId());
                payload.put("mobile", customer.getMobile());
                payload.put("description", "Your hostel owner wants to complete the KYC verifications. " +
                        "Please finish it at your earliest convenience");

                Message message = Message.builder()
                        .setToken(customerCredential.getFcmToken())
                        .putAllData(payload)
                        .build();

                try {
                    firebaseMessaging.send(message);
                } catch (FirebaseMessagingException ignored) {

                }
            }
        }
    }
}
