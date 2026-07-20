package com.smartstay.console.dto.billTemplates;

public record BillTemplatesDto(String prefix,
                               String suffix,
                               Double gstPercentile) {
}
