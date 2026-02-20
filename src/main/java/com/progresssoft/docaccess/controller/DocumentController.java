package com.progresssoft.docaccess.controller;

import com.progresssoft.docaccess.dto.request.BatchAccessRequest;
import com.progresssoft.docaccess.dto.request.CreateDocumentRequest;
import com.progresssoft.docaccess.dto.request.GrantPermissionRequest;
import com.progresssoft.docaccess.dto.response.BatchAccessResponse;
import com.progresssoft.docaccess.dto.response.DocumentResponse;
import com.progresssoft.docaccess.service.DocumentAccessService;
import com.progresssoft.docaccess.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final DocumentAccessService documentAccessService;

    @PostMapping
    public ResponseEntity<DocumentResponse> createDocument(
            @Valid @RequestBody CreateDocumentRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(documentService.createDocument(request));
    }

    @GetMapping
    public ResponseEntity<List<DocumentResponse>> getAllDocuments() {

        return ResponseEntity.ok(documentService.getAllAccessibleDocuments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse> getDocumentById(
            @PathVariable UUID id) {

        return ResponseEntity.ok(documentService.getDocumentById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable UUID id) {

        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/grant")
    public ResponseEntity<Void> grantPermission(
            @PathVariable UUID id,
            @Valid @RequestBody GrantPermissionRequest request) {

        documentAccessService.grantPermission(id, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/access-check")
    public ResponseEntity<BatchAccessResponse> batchAccessCheck(
            @Valid @RequestBody BatchAccessRequest request) {

        return ResponseEntity.ok(documentService.batchAccessCheck(request));
    }
}