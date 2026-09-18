package com.smartstay.console.responses.serviceToken;

public record ServiceTokenResponse(String service,
                                   String authToken,
                                   String secretValue,
                                   String expiryStatus,
                                   String expiryAtDate,
                                   String expiryAtTime) {
}
