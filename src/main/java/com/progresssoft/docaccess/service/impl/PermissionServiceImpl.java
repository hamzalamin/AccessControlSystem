package com.progresssoft.docaccess.service.impl;

import com.progresssoft.docaccess.enums.Permission;
import com.progresssoft.docaccess.repository.DocumentAccessRepository;
import com.progresssoft.docaccess.security.UserContextHolder;
import com.progresssoft.docaccess.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final DocumentAccessRepository repository;
    private static final String ADMIN = "admin";

    @Override
    public boolean isAdmin() {
        return ADMIN.equals(UserContextHolder.getCurrentUser());
    }

    @Override
    public boolean hasPermission(UUID documentId, Permission permission) {
        String currentUser = UserContextHolder.getCurrentUser();

        if (isAdmin()) return true;

        return repository.existsByDocumentIdAndUsernameAndPermission(
                documentId,
                currentUser,
                permission
        );
    }

    @Override
    public boolean canGrant(UUID documentId) {
        return isAdmin() || hasPermission(documentId, Permission.WRITE);
    }
}
