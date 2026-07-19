package com.smartstay.console.responses.customers;

import java.util.List;

public record CustomerDeductionsInfoRes(Double totalDeductionAmount,
                                        Double paidAmount,
                                        Double pendingAmount,
                                        List<DeductionsInfoRes> deductions) {
}
