package com.progresssoft.docaccess.service;

import com.progresssoft.docaccess.enums.Permission;

import java.util.UUID;

public interface PermissionService {
    public boolean isAdmin();
    public boolean hasPermission(UUID documentId, Permission permission);
    public boolean canGrant(UUID documentId);
}
