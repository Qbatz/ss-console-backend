package com.smartstay.console.services;

import com.smartstay.console.config.RestTemplateLoggingInterceptor;
import com.smartstay.console.exceptions.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class WhatsappService {

    @Value("${WHATSAPP_PHONE_NUMBER_ID}")
    private String whatsappPhoneNumberId;
    @Value("${WHATSAPP_ACCESS_TOKEN}")
    private String whatsappAccessToken;
    @Value("${WHATSAPP_PAYMENT_LINK_TEMPLATE_NAME}")
    private String whatsappPaymentLinkTemplateName;

    private final RestTemplate restTemplate;

    public WhatsappService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        restTemplate.setInterceptors(Collections.singletonList(new RestTemplateLoggingInterceptor()));
    }

    public void sendPaymentLink(String ownerName, String mobileNo, String paymentLink) {

        String url = "https://graph.facebook.com/v17.0/" + whatsappPhoneNumberId + "/messages";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + whatsappAccessToken);

        String formattedMobile = mobileNo.replaceAll("[^0-9]", "");
        formattedMobile = formattedMobile.replaceFirst("^0+", "");
        if (formattedMobile.length() == 10) {
            formattedMobile = "91" + formattedMobile;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("messaging_product", "whatsapp");
        body.put("to", formattedMobile);
        body.put("type", "template");

        Map<String, Object> template = new HashMap<>();
        template.put("name", whatsappPaymentLinkTemplateName);

        Map<String, String> language = new HashMap<>();
        language.put("code", "en");
        template.put("language", language);

        Map<String, Object> bodyComponent = new HashMap<>();
        bodyComponent.put("type", "body");
        bodyComponent.put("parameters", List.of(
                Map.of(
                        "type", "text",
                        "text", ownerName != null ? ownerName : "Client"
                )
        ));

        String normalizedLink = paymentLink.endsWith("/")
                ? paymentLink.substring(0, paymentLink.length() - 1)
                : paymentLink;

        String paymentId = normalizedLink.substring(
                normalizedLink.lastIndexOf('/') + 1
        );

        Map<String, Object> buttonComponent = new HashMap<>();
        buttonComponent.put("type", "button");
        buttonComponent.put("sub_type", "url");
        buttonComponent.put("index", "0");
        buttonComponent.put("parameters", List.of(
                Map.of(
                        "type", "text",
                        "text", paymentId
                )
        ));

        template.put("components", List.of(bodyComponent, buttonComponent));

        body.put("template", template);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(url, entity, String.class);
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }
    }
}
