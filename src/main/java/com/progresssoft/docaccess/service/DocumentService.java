package com.progresssoft.docaccess.service;

import com.progresssoft.docaccess.dto.BatchAccessRequest;
import com.progresssoft.docaccess.dto.BatchAccessResponse;
import com.progresssoft.docaccess.dto.CreateDocumentRequest;
import com.progresssoft.docaccess.dto.DocumentResponse;

import java.util.List;
import java.util.UUID;

public interface DocumentService {
    DocumentResponse createDocument(CreateDocumentRequest request);
    List<DocumentResponse> getAllAccessibleDocuments();
    DocumentResponse getDocumentById(UUID id);
    void deleteDocument(UUID id);
    BatchAccessResponse batchAccessCheck(BatchAccessRequest request);
}
