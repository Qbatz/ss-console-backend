package com.smartstay.console.responses.beds;

import com.smartstay.console.responses.date.DateFilterRes;

import java.util.List;

public record BedsInfoResWrapper(List<DateFilterRes> dateFilters,
                                 BedsInfoResponse bedInfo) {
}
