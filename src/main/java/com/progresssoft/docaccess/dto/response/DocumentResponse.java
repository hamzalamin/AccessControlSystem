package com.progresssoft.docaccess.dto.response;

import com.progresssoft.docaccess.dto.request.AccessibleUsersRequest;

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
