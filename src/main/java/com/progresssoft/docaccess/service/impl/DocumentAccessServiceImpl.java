package com.progresssoft.docaccess.service.impl;

import com.progresssoft.docaccess.dto.request.GrantPermissionRequest;
import com.progresssoft.docaccess.entity.Document;
import com.progresssoft.docaccess.entity.DocumentAccess;
import com.progresssoft.docaccess.exception.AccessDeniedException;
import com.progresssoft.docaccess.exception.DocumentNotFoundException;
import com.progresssoft.docaccess.repository.DocumentAccessRepository;
import com.progresssoft.docaccess.repository.DocumentRepository;
import com.progresssoft.docaccess.service.DocumentAccessService;
import com.progresssoft.docaccess.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DocumentAccessServiceImpl implements DocumentAccessService {

    private final DocumentRepository documentRepository;
    private final DocumentAccessRepository documentAccessRepository;
    private final PermissionService permissionService;

    @Override
    public void grantPermission(UUID documentId, GrantPermissionRequest request) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException(
                        "Document not found with id: " + documentId
                ));

        if (!permissionService.canGrant(documentId)) {
            throw new AccessDeniedException("You don't have permission to grant access");
        }

        boolean alreadyExists = documentAccessRepository
                .existsByDocumentIdAndUsernameAndPermission(
                        documentId,
                        request.username(),
                        request.permission()
                );

        if (alreadyExists) return;

        DocumentAccess access = DocumentAccess.builder()
                .username(request.username())
                .permission(request.permission())
                .document(document)
                .build();

        documentAccessRepository.save(access);
    }
}