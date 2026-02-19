package com.progresssoft.docaccess.dto;

import java.util.List;
import java.util.UUID;

public record DocumentResponse(
        UUID id,
        String name,
        String content,
        String fileType,
        List<AccessibleUsersRequest> accessibleUsers
) {
}
