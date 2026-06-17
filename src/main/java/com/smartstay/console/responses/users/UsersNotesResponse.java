package com.smartstay.console.responses.users;

public record UsersNotesResponse(Long notesId,
                                 String notes,
                                 String createdByUserType,
                                 String createdById,
                                 String createdBy,
                                 String createdAtDate,
                                 String createdAtTime) {
}
