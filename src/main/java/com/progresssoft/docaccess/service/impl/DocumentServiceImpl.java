package com.progresssoft.docaccess.service.impl;

import com.progresssoft.docaccess.dto.request.BatchAccessRequest;
import com.progresssoft.docaccess.dto.response.BatchAccessResponse;
import com.progresssoft.docaccess.dto.request.CreateDocumentRequest;
import com.progresssoft.docaccess.dto.response.DocumentResponse;
import com.progresssoft.docaccess.entity.Document;
import com.progresssoft.docaccess.enums.Permission;
import com.progresssoft.docaccess.exception.AccessDeniedException;
import com.progresssoft.docaccess.exception.DocumentNotFoundException;
import com.progresssoft.docaccess.mapper.DocumentMapper;
import com.progresssoft.docaccess.repository.DocumentRepository;
import com.progresssoft.docaccess.security.UserContextHolder;
import com.progresssoft.docaccess.service.DocumentService;
import com.progresssoft.docaccess.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final PermissionService permissionService;
    private final DocumentMapper documentMapper;

    public DocumentResponse createDocument(CreateDocumentRequest request) {
        if (!permissionService.isAdmin()) {
            throw new AccessDeniedException("Only admin can create documents");
        }

        Document document = documentMapper.toEntity(
                request,
                UserContextHolder.getCurrentUser()
        );

        return documentMapper.toResponse(documentRepository.save(document));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponse> getAllAccessibleDocuments() {
        if (permissionService.isAdmin()) {
            return documentMapper.toResponseList(documentRepository.findAll());
        }

        return documentMapper.toResponseList(
                documentRepository.findAllAccessibleByUsername(
                        UserContextHolder.getCurrentUser(),
                        Permission.READ
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentResponse getDocumentById(UUID id) {
        Document document = findDocumentOrThrow(id);

        if (!permissionService.hasPermission(id, Permission.READ)) {
            throw new AccessDeniedException("You don't have READ permission on this document");
        }

        return documentMapper.toResponse(document);
    }


    @Override
    public void deleteDocument(UUID id) {
        findDocumentOrThrow(id);

        if (!permissionService.hasPermission(id, Permission.DELETE)) {
            throw new AccessDeniedException("You don't have DELETE permission on this document");
        }

        documentRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BatchAccessResponse batchAccessCheck(BatchAccessRequest request) {
        if (permissionService.isAdmin()) {
            return new BatchAccessResponse(request.documentIds());
        }

        List<UUID> accessibleIds = documentRepository
                .findAllByIdInAndAccessListUsernameAndAccessListPermission(
                        request.documentIds(),
                        UserContextHolder.getCurrentUser(),
                        request.permission()
                )
                .stream()
                .toList();

        return new BatchAccessResponse(accessibleIds);
    }

    private Document findDocumentOrThrow(UUID id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException(
                        "Document not found with id: " + id
                ));
    }
}
