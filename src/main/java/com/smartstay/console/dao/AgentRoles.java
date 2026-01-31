package com.smartstay.console.dao;


import com.smartstay.console.handlers.RolesPermissionConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AgentRoles {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roleId;
    private String roleName;
    private Boolean isActive;
    private Boolean isDeleted;
    private Boolean isEditable;
    private Date createdAt;
    private Date updatedAt;

    @Column(columnDefinition = "TEXT")
    @Convert(converter = RolesPermissionConverter.class)
    private List<RolesPermission> permissions;
}
