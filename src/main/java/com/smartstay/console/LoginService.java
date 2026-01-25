package com.smartstay.console;

import com.smartstay.console.config.RestTemplateLoggingInterceptor;
import com.smartstay.console.dto.ZohoLoginResponse;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;

@Service
public class LoginService {

    private final RestTemplate restTemplate;

    public LoginService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        restTemplate.setInterceptors(Collections.singletonList(new RestTemplateLoggingInterceptor()));
    }

    public ResponseEntity<?> verifyAuthToken(String code, String location, String authorizeUrl) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Request body (form-data)
//        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
//        body.add("client_id", "1000.YLXF17CNZ2C016LL4WVTAQ8FRC6VWB");
//        body.add("client_secret", "d2415f2ec8b384d703b8eca441c0f92737d67627d8");
//        body.add("grant_type", "id_token");
//        body.add("redirect_uri", "http://localhost:8080/login/verify");
//        body.add("code", code);

        String endpoint = authorizeUrl + "/oauth/v2/token?";
        UriComponentsBuilder uriBuilder =
                UriComponentsBuilder.fromUriString(endpoint)
                        .queryParam("client_id", "1000.YLXF17CNZ2C016LL4WVTAQ8FRC6VWB")
                        .queryParam("client_secret", "d2415f2ec8b384d703b8eca441c0f92737d67627d8")
                        .queryParam("grant_type", "authorization_code")
                        .queryParam("redirect_uri", "http://localhost:5173/verify")
                        .queryParam("code", code);

        System.out.println(uriBuilder.toUriString());

        HttpEntity<Void> request =
                new HttpEntity<>(headers);

        ResponseEntity<ZohoLoginResponse> response = restTemplate.exchange(
                uriBuilder.toUriString(),
                HttpMethod.POST,
                request,
                ZohoLoginResponse.class
        );


        return new ResponseEntity<>(response.getBody(), HttpStatus.OK);
    }
}
