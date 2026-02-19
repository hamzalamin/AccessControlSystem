package com.progresssoft.docaccess.dto;


import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record CreateDocumentRequest(
        @NotBlank
        String name,
        @NotBlank
        String content,
        @NotBlank
        String fileType,
        List<AccessibleUsersRequest> accessibleUsers
) {
}
