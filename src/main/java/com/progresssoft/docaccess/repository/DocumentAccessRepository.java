package com.progresssoft.docaccess.repository;

import com.progresssoft.docaccess.entity.DocumentAccess;
import com.progresssoft.docaccess.enums.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentAccessRepository extends JpaRepository<DocumentAccess, UUID> {

    boolean existsByDocumentIdAndUsernameAndPermission(
            UUID documentId,
            String username,
            Permission permission
    );

    Optional<DocumentAccess> findByDocumentIdAndUsernameAndPermission(
            Long documentId,
            String username,
            Permission permission
    );

    List<DocumentAccess> findByUsernameAndPermissionAndDocumentIdIn(
            String username,
            Permission permission,
            List<Long> documentIds
    );
}
