package com.smartstay.console.dto.customers;

import java.util.Date;
import java.util.List;

public record AdvanceSnapshot(int id,
                              double advanceAmount,
                              double paidAmount,
                              Date invoiceDate,
                              Date dueDate,
                              String status,
                              Date createdAt,
                              Date updatedAt,
                              String createdBy,
                              List<DeductionsSnapshot> deductions,
                              String customerId) {
}
