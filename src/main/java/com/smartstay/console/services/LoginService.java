package com.smartstay.console.services;

import com.smartstay.console.config.RestTemplateLoggingInterceptor;
import com.smartstay.console.dao.Agent;
import com.smartstay.console.dto.zoho.ZohoLoginResponse;
import com.smartstay.console.dto.zoho.ZohoUserDetails;
import com.smartstay.console.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

@Service
public class LoginService {

    private final RestTemplate restTemplate;
    @Value("${REDIRECT-DOMAIN}")
    private String domain;
    @Autowired
    private AgentService agentService;
    @Autowired
    private JWTService jwtService;

    public LoginService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        restTemplate.setInterceptors(Collections.singletonList(new RestTemplateLoggingInterceptor()));
    }

    public ResponseEntity<?> verifyAuthToken(String code, String location, String authorizeUrl) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String endpoint = authorizeUrl + "/oauth/v2/token?";
        UriComponentsBuilder uriBuilder =
                UriComponentsBuilder.fromUriString(endpoint)
                        .queryParam("client_id", "1000.YLXF17CNZ2C016LL4WVTAQ8FRC6VWB")
                        .queryParam("client_secret", "d2415f2ec8b384d703b8eca441c0f92737d67627d8")
                        .queryParam("grant_type", "authorization_code")
                        .queryParam("redirect_uri", domain + "/verify")
                        .queryParam("code", code);


        HttpEntity<Void> request =
                new HttpEntity<>(headers);

        ResponseEntity<ZohoLoginResponse> response = restTemplate.exchange(
                uriBuilder.toUriString(),
                HttpMethod.POST,
                request,
                ZohoLoginResponse.class
        );


        if (response.getStatusCode() == HttpStatus.OK) {
            if (response.getBody().getId_token() != null) {
                Date expireAt = JwtUtil.getExpireAt(response.getBody().getId_token());
                ZohoUserDetails userDetails = JwtUtil.getUserDetails(response.getBody().getId_token());
                Agent agents = agentService.findAgentByEmail(userDetails.emailId());
                if (agents != null) {
                    if (agents.getIsProfileCompleted() != null && agents.getIsProfileCompleted()) {
                        HashMap<String, Object> claims = new HashMap<>();
                        claims.put("role", agents.getRoleId());
                        claims.put("zoho-user-id", agents.getAgentZohoUserId());
                        claims.put("agent-email", agents.getAgentEmailId());
                        String token = jwtService.generateToken(agents.getAgentId(), claims, expireAt);
                        return new ResponseEntity<>(token, HttpStatus.OK);
                    }
                    else {
                        Agent agents1 = agentService.updateProfileFromLogin(agents, userDetails);
                        HashMap<String, Object> claims = new HashMap<>();
                        claims.put("role", agents1.getRoleId());
                        claims.put("zoho-user-id", agents1.getAgentZohoUserId());
                        claims.put("agent-email", agents1.getAgentEmailId());
                        String token = jwtService.generateToken(agents1.getAgentId(), claims, expireAt);
                        return new ResponseEntity<>(token, HttpStatus.OK);
                    }

                } else {
                    return new ResponseEntity<>("Not having access to portal", HttpStatus.FORBIDDEN);
                }
            }

            else {
                return new ResponseEntity<>("Not having access to portal", HttpStatus.FORBIDDEN);
            }
        }


        return new ResponseEntity<>("Not having access to portal", HttpStatus.FORBIDDEN);
    }
}
