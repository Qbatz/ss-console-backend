package com.smartstay.console.responses.serviceToken;

public record ServiceTokenResponse(String service,
                                   String serviceName,
                                   String authToken,
                                   String secretValue,
                                   String expiryStatus,
                                   String expiryAtDate,
                                   String expiryAtTime) {
}
