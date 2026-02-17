package com.smartstay.console.dao;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RolesPermission {
    private int moduleId;
    private boolean canRead;
    private boolean canWrite;
    private boolean canDelete;
    private boolean canUpdate;

}
