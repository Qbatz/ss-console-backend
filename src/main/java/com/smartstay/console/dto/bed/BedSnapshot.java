package com.smartstay.console.dto.bed;

import java.util.Date;

public record BedSnapshot(Integer bedId,
                          String bedName,
                          Boolean isActive,
                          Boolean isDeleted,
                          Date createdAt,
                          Date updatedAt,
                          String parentId,
                          Integer roomId,
                          String hostelId,
                          boolean isBooked,
                          double rentAmount,
                          String status,
                          String currentStatus,
                          Date freeFrom) {
}
