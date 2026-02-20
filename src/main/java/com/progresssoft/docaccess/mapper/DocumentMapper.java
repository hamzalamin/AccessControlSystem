package com.progresssoft.docaccess.mapper;

import com.progresssoft.docaccess.dto.request.AccessibleUsersRequest;
import com.progresssoft.docaccess.dto.request.CreateDocumentRequest;
import com.progresssoft.docaccess.dto.response.DocumentResponse;
import com.progresssoft.docaccess.entity.Document;
import com.progresssoft.docaccess.entity.DocumentAccess;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class DocumentMapper {

    public DocumentResponse toResponse(Document document) {
        return new DocumentResponse(
                document.getId(),
                document.getName(),
                document.getContent(),
                document.getFileType(),
                toAccessibleUsersList(document.getAccessList())
        );
    }

    public Document toEntity(CreateDocumentRequest request, String createdBy) {
        Document document = Document.builder()
                .name(request.name())
                .content(request.content())
                .fileType(request.fileType())
                .createdBy(createdBy)
                .build();

        document.setAccessList(toDocumentAccessList(request.accessibleUsers(), document));

        return document;
    }

    public List<DocumentResponse> toResponseList(List<Document> documents) {
        return documents.stream()
                .map(this::toResponse)
                .toList();
    }

    private AccessibleUsersRequest toAccessibleUser(DocumentAccess access) {
        return new AccessibleUsersRequest(
                access.getUsername(),
                access.getPermission()
        );
    }

    private List<DocumentAccess> toDocumentAccessList(List<AccessibleUsersRequest> accessibleUsers, Document document) {
        return accessibleUsers.stream()
                .map(accessibleUsersRequest -> toDocumentAccess(accessibleUsersRequest, document))
                .toList();
    }

    private DocumentAccess toDocumentAccess(AccessibleUsersRequest request, Document document) {
        return DocumentAccess.builder()
                .permission(request.permission())
                .username(request.username())
                .document(document)
                .build();
    }

    private List<AccessibleUsersRequest> toAccessibleUsersList(List<DocumentAccess> accessList) {
        if (accessList == null || accessList.isEmpty()) {
            return Collections.emptyList();
        }
        return accessList.stream()
                .map(this::toAccessibleUser)
                .toList();
    }

}
