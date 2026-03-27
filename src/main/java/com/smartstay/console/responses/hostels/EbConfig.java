package com.smartstay.console.responses.hostels;

public record EbConfig(Integer ebConfigId,
                       boolean shouldIncludeInRent,
                       String typeOfReading,
                       Double charge,
                       Integer billDate) {
}
