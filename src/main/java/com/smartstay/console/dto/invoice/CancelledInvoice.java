package com.smartstay.console.dto.invoice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CancelledInvoice {
    private String invoiceId;
    private String paymentStatus;
}
