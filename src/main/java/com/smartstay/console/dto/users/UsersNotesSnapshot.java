package com.smartstay.console.dto.users;

import java.util.Date;

public record UsersNotesSnapshot(Long id,
                                 String comment,
                                 String createdByUserType,
                                 String createdBy,
                                 Date createdAt,
                                 String userId,
                                 String parentId) {
}
