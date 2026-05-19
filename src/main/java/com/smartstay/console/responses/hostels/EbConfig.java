package com.smartstay.console.responses.hostels;

public record EbConfig(Integer ebConfigId,
                       boolean shouldIncludeInRent,
                       String typeOfReading,
                       boolean showFlatCharge,
                       Double charge,
                       Double flatCharge,
                       Integer billDate,
                       boolean isUpdated,
                       String lastUpdatedAtDate,
                       String lastUpdatedAtTime,
                       String updatedBy) {
}
