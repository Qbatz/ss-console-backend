package com.smartstay.console.services;

import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.Agent;
import com.smartstay.console.dao.Credentials;
import com.smartstay.console.dto.credentials.CredentialsSnapshot;
import com.smartstay.console.ennum.ActivityType;
import com.smartstay.console.ennum.ServiceEnum;
import com.smartstay.console.ennum.ServiceTokenStatus;
import com.smartstay.console.ennum.Source;
import com.smartstay.console.payloads.serviceToken.GenerateServiceTokenPayload;
import com.smartstay.console.responses.serviceToken.ServiceTokenResponse;
import com.smartstay.console.responses.serviceToken.ServiceTokenStatusResponse;
import com.smartstay.console.responses.serviceToken.ServicesResponse;
import com.smartstay.console.utils.SnapshotUtility;
import com.smartstay.console.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.*;

@Service
public class ServiceTokenService {

    @Autowired
    private Authentication authentication;
    @Autowired
    private AgentService agentService;
    @Autowired
    private AgentActivitiesService agentActivitiesService;
    @Autowired
    private CredentialsService credentialsService;
    @Autowired
    private JWTService jwtService;

    private final SecureRandom secureRandom = new SecureRandom();

    public ResponseEntity<?> getServices() {

        List<ServicesResponse> responses = Arrays.stream(ServiceEnum.values())
                .map(i -> new ServicesResponse(i.name()))
                .toList();

        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    public ResponseEntity<?> getSecret() {

        String secret = generateSecret();

        return new ResponseEntity<>(secret, HttpStatus.OK);
    }

    public String generateSecret() {
        byte[] secret = new byte[32];
        secureRandom.nextBytes(secret);

        return Base64.getEncoder()
                .withoutPadding()
                .encodeToString(secret);
    }

    public ResponseEntity<?> generateServiceToken(GenerateServiceTokenPayload payload) {

        String loggedInAgentId = authentication.getName();
        Agent loggedInAgent = agentService.findUserByUserId(loggedInAgentId);
        if (loggedInAgent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        String service = payload.service();
        if (credentialsService.existsByService(service)){
            return new ResponseEntity<>("Service already exists", HttpStatus.BAD_REQUEST);
        }

        String secret = null;
        if (payload.secret() != null && !payload.secret().isBlank()){
            secret = payload.secret();
        } else {
            secret = generateSecret();
        }

        Date today = new Date();

        Date expiryDate = null;
        if (payload.expiryDate() != null && payload.expiryTime() != null){
            expiryDate = Utils.localDateTimeToDate(payload.expiryDate(), payload.expiryTime());
        }

        if (expiryDate != null && !expiryDate.after(today)) {
            return new ResponseEntity<>("Expiry date must be in the future", HttpStatus.BAD_REQUEST);
        }

        if (expiryDate == null){
            expiryDate = Date.from(today.toInstant().plus(Duration.ofDays(7)));
        }

        HashMap<String, Object> claims = new HashMap<>();

        claims.put("service", service);
        claims.put("issued-at", today);
        claims.put("expiry-at", expiryDate);

        String token = jwtService.generateTokenByDate(service, secret, claims, today, expiryDate);

        Credentials credentials = new Credentials();

        credentials.setService(service);
        credentials.setAuthToken(token);
        credentials.setSecretValue(secret);

        credentials = credentialsService.save(credentials);

        CredentialsSnapshot newSnapshot = SnapshotUtility.toSnapshot(credentials);

        agentActivitiesService.createAgentActivity(loggedInAgent, ActivityType.CREATE, Source.CREDENTIALS,
                credentials.getService(), null, newSnapshot);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    public ResponseEntity<?> reGenerateServiceToken(GenerateServiceTokenPayload payload) {

        String loggedInAgentId = authentication.getName();
        Agent loggedInAgent = agentService.findUserByUserId(loggedInAgentId);
        if (loggedInAgent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        String service = payload.service();
        Credentials credentials = credentialsService.getByService(service);
        if (credentials == null) {
            return new ResponseEntity<>(Utils.CREDENTIALS_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        if (ServiceEnum.zoho.name().equals(service)) {
            return new ResponseEntity<>("Zoho service token can not be updated", HttpStatus.BAD_REQUEST);
        }

        CredentialsSnapshot oldSnapshot = SnapshotUtility.toSnapshot(credentials);

        String secret = null;
        if (payload.secret() != null && !payload.secret().isBlank()){
            secret = payload.secret();
        } else {
            secret = generateSecret();
        }

        Date today = new Date();

        Date expiryDate = null;
        if (payload.expiryDate() != null && payload.expiryTime() != null){
            expiryDate = Utils.localDateTimeToDate(payload.expiryDate(), payload.expiryTime());
        }

        if (expiryDate != null && !expiryDate.after(today)) {
            return new ResponseEntity<>("Expiry date must be in the future", HttpStatus.BAD_REQUEST);
        }

        if (expiryDate == null){
            expiryDate = Date.from(today.toInstant().plus(Duration.ofDays(7)));
        }

        HashMap<String, Object> claims = new HashMap<>();

        claims.put("service", service);
        claims.put("issued-at", today);
        claims.put("expiry-at", expiryDate);

        String token = jwtService.generateTokenByDate(service, secret, claims, today, expiryDate);

        credentials.setAuthToken(token);
        credentials.setSecretValue(secret);

        credentials = credentialsService.save(credentials);

        CredentialsSnapshot newSnapshot = SnapshotUtility.toSnapshot(credentials);

        agentActivitiesService.createAgentActivity(loggedInAgent, ActivityType.UPDATE, Source.CREDENTIALS,
                credentials.getService(), oldSnapshot, newSnapshot);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    public ResponseEntity<?> revokeServiceToken(String service) {

        String loggedInAgentId = authentication.getName();
        Agent loggedInAgent = agentService.findUserByUserId(loggedInAgentId);
        if (loggedInAgent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Credentials credentials = credentialsService.getByService(service);
        if (credentials == null) {
            return new ResponseEntity<>(Utils.CREDENTIALS_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        if (ServiceEnum.zoho.name().equals(service)) {
            return new ResponseEntity<>("Zoho service token can not be updated", HttpStatus.BAD_REQUEST);
        }

        CredentialsSnapshot oldSnapshot = SnapshotUtility.toSnapshot(credentials);

        Date today = new Date();

        HashMap<String, Object> claims = new HashMap<>();

        claims.put("service", service);
        claims.put("issued-at", today);
        claims.put("expiry-at", today);

        String token = jwtService.generateTokenByDate(service,
                credentials.getSecretValue(), claims, today, today);

        credentials.setAuthToken(token);

        credentials = credentialsService.save(credentials);

        CredentialsSnapshot newSnapshot = SnapshotUtility.toSnapshot(credentials);

        agentActivitiesService.createAgentActivity(loggedInAgent, ActivityType.UPDATE, Source.CREDENTIALS,
                credentials.getService(), oldSnapshot, newSnapshot);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    public ResponseEntity<?> getServiceTokens(int page, int size, String name, String status) {

        String loggedInAgentId = authentication.getName();
        Agent loggedInAgent = agentService.findUserByUserId(loggedInAgentId);
        if (loggedInAgent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        name = (name == null || name.isBlank()) ? null : name.trim();
        status = (status == null || status.isBlank()) ? null : status.trim();

        if (status != null && status.equals("ALL")){
            status = null;
        }

        page = Math.max(page - 1, 0);
        size = Math.max(size, 1);

        List<Credentials> credentials = credentialsService.getAllCredentials();

        List<ServiceTokenResponse> allTokens = credentials.stream()
                .map(credential -> {

                    Date expiryDate = null;

                    try {
                        expiryDate = jwtService.getExpiryDate(
                                credential.getAuthToken(),
                                credential.getSecretValue()
                        );
                    } catch (Exception e) {
                        // ignored
                    }

                    String expiryAtDate = null;
                    String expiryAtTime = null;
                    String expiryStatus = null;

                    if (expiryDate != null) {
                        ServiceTokenStatus tokenStatus = getTokenStatus(expiryDate);

                        if (tokenStatus != null) {
                            expiryStatus = tokenStatus.name();
                        }

                        expiryAtDate = Utils.dateToString(expiryDate);
                        expiryAtTime = Utils.dateToTime(expiryDate);
                    }

                    return new ServiceTokenResponse(
                            credential.getService(),
                            credential.getAuthToken(),
                            credential.getSecretValue(),
                            expiryStatus,
                            expiryAtDate,
                            expiryAtTime
                    );
                })
                .toList();

        // Counts BEFORE any filters
        long totalCount = allTokens.size();

        long activeCount = allTokens.stream()
                .filter(token -> ServiceTokenStatus.ACTIVE.name()
                        .equals(token.expiryStatus()))
                .count();

        long expiringSoonCount = allTokens.stream()
                .filter(token -> ServiceTokenStatus.EXPIRING_SOON.name()
                        .equals(token.expiryStatus()))
                .count();

        long expiredCount = allTokens.stream()
                .filter(token -> ServiceTokenStatus.EXPIRED.name()
                        .equals(token.expiryStatus()))
                .count();

        // Name filter
        String finalName = name;
        List<ServiceTokenResponse> filteredTokens = allTokens.stream()
                .filter(token -> finalName == null ||
                        Utils.normalizeName(token.service()).contains(Utils.normalizeName(finalName)))
                .toList();

        // Status filter
        String finalStatus = status;
        filteredTokens = filteredTokens.stream()
                .filter(token -> finalStatus == null ||
                        finalStatus.equalsIgnoreCase(token.expiryStatus()))
                .toList();

        int totalItems = filteredTokens.size();

        int totalPages = (int) Math.ceil((double) totalItems / size);

        int fromIndex = page * size;

        List<ServiceTokenResponse> pageContent;

        if (fromIndex >= totalItems) {
            pageContent = List.of();
        } else {
            int toIndex = Math.min(fromIndex + size, totalItems);
            pageContent = filteredTokens.subList(fromIndex, toIndex);
        }

        List<ServiceTokenStatusResponse> expiryStatusFilters = Arrays.stream(ServiceTokenStatus.values())
                .map(i -> new ServiceTokenStatusResponse(i.name(), i.getLabel()))
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("serviceTokens", pageContent);
        response.put("totalCount", totalCount);
        response.put("activeCount", activeCount);
        response.put("expiringSoonCount", expiringSoonCount);
        response.put("expiredCount", expiredCount);
        response.put("currentPage", page + 1);
        response.put("pageSize", size);
        response.put("totalItems", totalItems);
        response.put("totalPages", totalPages);
        response.put("expiryStatusFilters", expiryStatusFilters);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private ServiceTokenStatus getTokenStatus(Date expiryDate) {

        Date now = new Date();

        if (!expiryDate.after(now)) {
            return ServiceTokenStatus.EXPIRED;
        }

        Date expiringSoonThreshold = Date.from(
                now.toInstant().plus(Duration.ofDays(2))
        );

        if (!expiryDate.after(expiringSoonThreshold)) {
            return ServiceTokenStatus.EXPIRING_SOON;
        }

        return ServiceTokenStatus.ACTIVE;
    }

    public ResponseEntity<?> getServiceTokenByService(String service) {

        String loggedInAgentId = authentication.getName();
        Agent loggedInAgent = agentService.findUserByUserId(loggedInAgentId);
        if (loggedInAgent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Credentials credential = credentialsService.getByService(service);
        if (credential == null) {
            return new ResponseEntity<>(Utils.CREDENTIALS_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        Date expiryDate = null;

        try {
            expiryDate = jwtService.getExpiryDate(
                    credential.getAuthToken(),
                    credential.getSecretValue()
            );
        } catch (Exception e) {
            // ignored
        }

        String expiryAtDate = null;
        String expiryAtTime = null;
        String expiryStatus = null;

        if (expiryDate != null) {
            ServiceTokenStatus tokenStatus = getTokenStatus(expiryDate);

            if (tokenStatus != null) {
                expiryStatus = tokenStatus.name();
            }

            expiryAtDate = Utils.dateToString(expiryDate);
            expiryAtTime = Utils.dateToTime(expiryDate);
        }

        ServiceTokenResponse response = new ServiceTokenResponse(credential.getService(),
                credential.getAuthToken(), credential.getSecretValue(), expiryStatus,
                expiryAtDate, expiryAtTime);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
