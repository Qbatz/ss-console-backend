package com.smartstay.console.services;

import com.smartstay.console.config.RestTemplateLoggingInterceptor;
import com.smartstay.console.exceptions.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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

    public void sendPaymentLink(String mobileNo, String paymentLink) {

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

        Map<String, String> parameter = new HashMap<>();
        parameter.put("type", "text");
        parameter.put("text", paymentLink != null ? paymentLink : "Error in payment link");

        bodyComponent.put("parameters", Collections.singletonList(parameter));
        template.put("components", Collections.singletonList(bodyComponent));

        body.put("template", template);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(url, entity, String.class);
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }
    }
}
