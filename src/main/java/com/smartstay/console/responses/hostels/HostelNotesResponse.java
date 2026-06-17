package com.smartstay.console.responses.hostels;

public record HostelNotesResponse(Long notesId,
                                  String notes,
                                  String createdByUserType,
                                  String createdById,
                                  String createdBy,
                                  String createdAtDate,
                                  String createdAtTime) {
}
