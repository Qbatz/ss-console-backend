package com.smartstay.console.dto.credentials;

public record CredentialsSnapshot(String service,
                                  String clientId,
                                  String authToken,
                                  String secretValue,
                                  String refreshToken,
                                  String otherSecrets) {
}
