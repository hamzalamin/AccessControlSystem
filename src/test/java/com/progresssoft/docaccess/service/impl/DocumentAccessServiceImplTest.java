package com.progresssoft.docaccess.service.impl;

import com.progresssoft.docaccess.dto.request.GrantPermissionRequest;
import com.progresssoft.docaccess.entity.Document;
import com.progresssoft.docaccess.entity.DocumentAccess;
import com.progresssoft.docaccess.enums.Permission;
import com.progresssoft.docaccess.exception.AccessDeniedException;
import com.progresssoft.docaccess.exception.DocumentNotFoundException;
import com.progresssoft.docaccess.repository.DocumentAccessRepository;
import com.progresssoft.docaccess.repository.DocumentRepository;
import com.progresssoft.docaccess.security.UserContextHolder;
import com.progresssoft.docaccess.service.PermissionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DocumentAccessService Tests")
class DocumentAccessServiceImplTest {

    @Mock private DocumentRepository documentRepository;
    @Mock private DocumentAccessRepository documentAccessRepository;
    @Mock private PermissionService permissionService;

    @InjectMocks
    private DocumentAccessServiceImpl documentAccessService;

    private final UUID documentId = UUID.randomUUID();

    @AfterEach
    void clearContext() {
        UserContextHolder.clear();
    }

    private Document buildDocument() {
        Document doc = new Document();
        doc.setId(documentId);
        doc.setName("Test Document");
        doc.setContent("Content");
        doc.setFileType("pdf");
        doc.setAccessList(Collections.emptyList());
        return doc;
    }

    @Nested
    @DisplayName("grantPermission()")
    class GrantPermission {

        @Test
        @DisplayName("grants permission successfully when user is admin")
        void grantsPermission_whenAdmin() {
            UserContextHolder.setCurrentUser("admin");
            GrantPermissionRequest request = new GrantPermissionRequest("user3", Permission.READ);
            Document document = buildDocument();

            when(documentRepository.findById(documentId)).thenReturn(Optional.of(document));
            when(permissionService.canGrant(documentId)).thenReturn(true);
            when(documentAccessRepository.existsByDocumentIdAndUsernameAndPermission(
                    documentId, "user3", Permission.READ
            )).thenReturn(false);

            documentAccessService.grantPermission(documentId, request);

            verify(documentAccessRepository).save(any(DocumentAccess.class));
        }

        @Test
        @DisplayName("grants permission successfully when user has WRITE permission")
        void grantsPermission_whenUserHasWritePermission() {
            UserContextHolder.setCurrentUser("user1");
            GrantPermissionRequest request = new GrantPermissionRequest("user4", Permission.READ);
            Document document = buildDocument();

            when(documentRepository.findById(documentId)).thenReturn(Optional.of(document));
            when(permissionService.canGrant(documentId)).thenReturn(true);
            when(documentAccessRepository.existsByDocumentIdAndUsernameAndPermission(
                    documentId, "user4", Permission.READ
            )).thenReturn(false);

            documentAccessService.grantPermission(documentId, request);

            verify(documentAccessRepository).save(any(DocumentAccess.class));
        }

        @Test
        @DisplayName("saves correct access entity when granting permission")
        void savesCorrectAccessEntity() {
            UserContextHolder.setCurrentUser("admin");
            GrantPermissionRequest request = new GrantPermissionRequest("user3", Permission.WRITE);
            Document document = buildDocument();

            when(documentRepository.findById(documentId)).thenReturn(Optional.of(document));
            when(permissionService.canGrant(documentId)).thenReturn(true);
            when(documentAccessRepository.existsByDocumentIdAndUsernameAndPermission(
                    documentId, "user3", Permission.WRITE
            )).thenReturn(false);

            documentAccessService.grantPermission(documentId, request);

            ArgumentCaptor<DocumentAccess> captor = ArgumentCaptor.forClass(DocumentAccess.class);
            verify(documentAccessRepository).save(captor.capture());

            DocumentAccess saved = captor.getValue();
            assertThat(saved.getUsername()).isEqualTo("user3");
            assertThat(saved.getPermission()).isEqualTo(Permission.WRITE);
            assertThat(saved.getDocument()).isEqualTo(document);
        }

        @Test
        @DisplayName("skips saving when permission already exists (idempotent)")
        void skipsGranting_whenPermissionAlreadyExists() {
            UserContextHolder.setCurrentUser("admin");
            GrantPermissionRequest request = new GrantPermissionRequest("user1", Permission.READ);
            Document document = buildDocument();

            when(documentRepository.findById(documentId)).thenReturn(Optional.of(document));
            when(permissionService.canGrant(documentId)).thenReturn(true);
            when(documentAccessRepository.existsByDocumentIdAndUsernameAndPermission(
                    documentId, "user1", Permission.READ
            )).thenReturn(true);

            documentAccessService.grantPermission(documentId, request);

            verify(documentAccessRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws DocumentNotFoundException when document does not exist")
        void throwsDocumentNotFoundException_whenDocumentNotFound() {
            GrantPermissionRequest request = new GrantPermissionRequest("user3", Permission.READ);

            when(documentRepository.findById(documentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> documentAccessService.grantPermission(documentId, request))
                    .isInstanceOf(DocumentNotFoundException.class)
                    .hasMessageContaining(documentId.toString());

            verifyNoInteractions(permissionService);
            verifyNoInteractions(documentAccessRepository);
        }

        @Test
        @DisplayName("throws AccessDeniedException when user has no WRITE permission")
        void throwsAccessDeniedException_whenNoWritePermission() {
            UserContextHolder.setCurrentUser("user1");
            GrantPermissionRequest request = new GrantPermissionRequest("user3", Permission.READ);
            Document document = buildDocument();

            when(documentRepository.findById(documentId)).thenReturn(Optional.of(document));
            when(permissionService.canGrant(documentId)).thenReturn(false);

            assertThatThrownBy(() -> documentAccessService.grantPermission(documentId, request))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("You don't have permission to grant access");

            verify(documentAccessRepository, never()).save(any());
        }
    }
}