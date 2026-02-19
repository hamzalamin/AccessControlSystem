package com.progresssoft.docaccess.dto;

import com.progresssoft.docaccess.enums.Permission;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GrantPermissionRequest(

        @NotBlank(message = "Username is required")
        String username,

        @NotNull(message = "Permission is required")
        Permission permission
) {}
