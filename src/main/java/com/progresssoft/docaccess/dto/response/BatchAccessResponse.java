package com.progresssoft.docaccess.dto.response;

import java.util.List;
import java.util.UUID;

public record BatchAccessResponse(
        List<UUID> accessibleIds
) {}