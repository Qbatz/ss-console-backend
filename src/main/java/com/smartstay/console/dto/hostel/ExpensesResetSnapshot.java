package com.smartstay.console.dto.hostel;

import com.smartstay.console.dao.ExpensesV1;

import java.util.List;

public record ExpensesResetSnapshot(List<ExpensesV1> expenses) {
}
