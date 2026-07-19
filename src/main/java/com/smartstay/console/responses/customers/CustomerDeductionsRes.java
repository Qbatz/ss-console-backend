package com.smartstay.console.responses.customers;

import java.util.List;

public record CustomerDeductionsRes(String customerId,
                                    String hostelId,
                                    String hostelName,
                                    String customerName,
                                    String customerBedStatus,
                                    String customerCurrentStatus,
                                    List<DeductionsRes> customerAdvanceDeductions,
                                    List<InvoiceDeductionsRes> advanceInvoice) {
}
