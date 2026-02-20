package com.progresssoft.docaccess.service;

import com.progresssoft.docaccess.dto.request.BatchAccessRequest;
import com.progresssoft.docaccess.dto.response.BatchAccessResponse;
import com.progresssoft.docaccess.dto.request.CreateDocumentRequest;
import com.progresssoft.docaccess.dto.response.DocumentResponse;

import java.util.List;
import java.util.UUID;

public interface DocumentService {
    DocumentResponse createDocument(CreateDocumentRequest request);
    List<DocumentResponse> getAllAccessibleDocuments();
    DocumentResponse getDocumentById(UUID id);
    void deleteDocument(UUID id);
    BatchAccessResponse batchAccessCheck(BatchAccessRequest request);
}
