package com.smartstay.console.dto.kycDetails;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DigioInitiateKycRequest(@JsonProperty("customer_identifier")
                                      String customerIdentifier,

                                      @JsonProperty("notify_customer")
                                      Boolean notifyCustomer,

                                      @JsonProperty("customer_notification_mode")
                                      String customerNotificationMode,

                                      @JsonProperty("customer_name")
                                      String customerName,

                                      @JsonProperty("template_name")
                                      String templateName,

                                      @JsonProperty("generate_access_token")
                                      Boolean generateAccessToken) {
}
