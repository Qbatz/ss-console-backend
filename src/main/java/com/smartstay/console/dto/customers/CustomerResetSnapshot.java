package com.smartstay.console.dto.customers;

public record CustomerResetSnapshot(CustomersSnapshot customer,
                                    CustomersCredentialsSnapshot customerCredentials) {
}
