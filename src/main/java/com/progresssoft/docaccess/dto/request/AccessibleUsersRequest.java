package com.progresssoft.docaccess.dto.request;


import com.progresssoft.docaccess.enums.Permission;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AccessibleUsersRequest(
        @NotBlank
        String username,
        @NotNull
        Permission permission
) {
}
