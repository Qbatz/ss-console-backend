package com.smartstay.console.dto.hostel;

import java.util.Date;

public record HostelNotesSnapshot(Long id,
                                  String comment,
                                  String createdByUserType,
                                  String createdBy,
                                  Date createdAt,
                                  String hostelId,
                                  String parentId) {
}
