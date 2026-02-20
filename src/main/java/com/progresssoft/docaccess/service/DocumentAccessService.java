package com.progresssoft.docaccess.service;

import com.progresssoft.docaccess.dto.request.GrantPermissionRequest;

import java.util.UUID;

public interface DocumentAccessService {
    void grantPermission(UUID documentId, GrantPermissionRequest request);
}
