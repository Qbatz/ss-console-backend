package com.smartstay.console.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.smartstay.console.dto.zoho.ZohoUserDetails;
import tools.jackson.databind.ObjectMapper;

import java.util.Base64;
import java.util.Date;
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

    public static String extractUserDetails(String idToken) {
        DecodedJWT jwt = JWT.decode(idToken);

        String email = jwt.getClaim("email").asString();
        String firstName = jwt.getClaim("first_name").asString();
        String lastName = jwt.getClaim("last_name").asString();
        String fullName = jwt.getClaim("name").asString();
        String zohoUserId = jwt.getSubject();

        System.out.println(email);
        System.out.println(fullName);
        System.out.println(zohoUserId);

        return email;
    }

    public static ZohoUserDetails getUserDetails(String idToken) {
        DecodedJWT jwt = JWT.decode(idToken);
        String sub = jwt.getSubject();
        String email = jwt.getClaim("email").asString();
        String firstName = jwt.getClaim("first_name").asString();
        String lastName = jwt.getClaim("last_name").asString();
        String fullName = jwt.getClaim("name").asString();
        String zohoUserId = jwt.getSubject();

        System.out.println(email);
        System.out.println(fullName);
        System.out.println(zohoUserId);

        return new ZohoUserDetails(email, sub, firstName, lastName, fullName);
    }

    public static Date getExpireAt(String idToken) {
        DecodedJWT jwt = JWT.decode(idToken);
        return jwt.getExpiresAt();
    }
}
