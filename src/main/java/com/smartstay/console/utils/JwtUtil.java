package com.smartstay.console.utils;

import tools.jackson.databind.ObjectMapper;

import java.util.Base64;
import java.util.Map;

public class JwtUtil {
    public static String extractEmail(String idToken) throws Exception {
        String[] parts = idToken.split("\\.");
        String payload = parts[1];

        byte[] decodedBytes = Base64.getUrlDecoder().decode(payload);
        String json = new String(decodedBytes);

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> payloadMap = mapper.readValue(json, Map.class);

        return (String) payloadMap.get("email");
    }
}
