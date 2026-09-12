package com.smartstay.console.dto.eb;

import com.smartstay.console.dao.CustomersBedHistory;

import java.util.Date;

public record RoomEbAllocation(CustomersBedHistory bedHistory,
                               Date startDate,
                               Date endDate,
                               long overlapDays) {
}
