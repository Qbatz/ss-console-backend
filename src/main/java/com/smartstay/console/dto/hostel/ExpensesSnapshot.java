package com.smartstay.console.dto.hostel;

import java.util.Date;

public record ExpensesSnapshot(String expenseId,
                               Long categoryId,
                               Long subCategoryId,
                               String parentId,
                               String hostelId,
                               String bankId,
                               Double unitPrice,
                               Integer unitCount,
                               Double totalPrice,
                               Double gst,
                               Double cgst,
                               Double sgst,
                               Double gstAmount,
                               Double cgstAmount,
                               Double sgstAmount,
                               Double discounts,
                               Double discountAmount,
                               String expenseNumber,
                               Double transactionAmount,
                               String vendorId,
                               String source,
                               Date transactionDate,
                               Date createdAt,
                               Date updatedAt,
                               String createdBy,
                               boolean isActive,
                               String description) {
}
