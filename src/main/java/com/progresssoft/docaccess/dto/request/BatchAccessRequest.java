package com.progresssoft.docaccess.dto.request;

import com.progresssoft.docaccess.enums.Permission;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record BatchAccessRequest(

        @NotNull(message = "Permission is required")
        Permission permission,

        @NotEmpty(message = "Document IDs cannot be empty")
        List<UUID> documentIds
) {}