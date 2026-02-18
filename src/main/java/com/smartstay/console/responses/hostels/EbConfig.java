package com.smartstay.console.responses.hostels;

public record EbConfig(Integer ebConfigId,
                       boolean shouldIncludeInRent,
                       String typeOfReading,
                       boolean isProRate,
                       Double charge,
                       Integer billDate) {
}
