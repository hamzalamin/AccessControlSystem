package com.progresssoft.docaccess.mapper;

import com.progresssoft.docaccess.dto.AccessibleUsersRequest;
import com.progresssoft.docaccess.dto.CreateDocumentRequest;
import com.progresssoft.docaccess.dto.DocumentResponse;
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
        return Document.builder()
                .name(request.name())
                .content(request.content())
                .fileType(request.fileType())
                .createdBy(createdBy)
                .build();
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

    private List<AccessibleUsersRequest> toAccessibleUsersList(List<DocumentAccess> accessList) {
        if (accessList == null || accessList.isEmpty()) {
            return Collections.emptyList();
        }
        return accessList.stream()
                .map(this::toAccessibleUser)
                .toList();
    }

}
