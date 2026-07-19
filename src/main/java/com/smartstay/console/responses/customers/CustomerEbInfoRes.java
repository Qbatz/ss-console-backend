package com.smartstay.console.responses.customers;

import java.util.List;

public record CustomerEbInfoRes(Double lastReading,
                                Double unitPrice,
                                String lastEntryDate,
                                String typeOfReading,
                                Double pendingEbAmount,
                                boolean isHostelReading,
                                boolean canAddEb,
                                List<MissedEbRoomsRes> missedEb,
                                List<PendingEbRes> pendingEb) {
}
