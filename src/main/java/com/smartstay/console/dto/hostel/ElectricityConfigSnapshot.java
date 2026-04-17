package com.smartstay.console.dto.hostel;

import java.util.Date;

public record ElectricityConfigSnapshot(Integer ebConfigId,
                                        boolean shouldIncludeInRent,
                                        String typeOfReading,
                                        Date lastUpdate,
                                        String updatedBy,
                                        Double charge,
                                        Double flatCharge,
                                        boolean isUpdated,
                                        Integer billDate,
                                        String hostelId) {
}
