package com.smartstay.console.payloads.customers;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public record CustomerDatePayload(@JsonFormat(pattern = "dd-MM-yyyy")
                                  LocalDate date) {
}
