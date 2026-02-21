package com.progresssoft.docaccess.service.impl;

import com.progresssoft.docaccess.dto.request.BatchAccessRequest;
import com.progresssoft.docaccess.dto.request.CreateDocumentRequest;
import com.progresssoft.docaccess.dto.response.BatchAccessResponse;
import com.progresssoft.docaccess.dto.response.DocumentResponse;
import com.progresssoft.docaccess.entity.Document;
import com.progresssoft.docaccess.enums.Permission;
import com.progresssoft.docaccess.exception.AccessDeniedException;
import com.progresssoft.docaccess.exception.DocumentNotFoundException;
import com.progresssoft.docaccess.mapper.DocumentMapper;
import com.progresssoft.docaccess.repository.DocumentRepository;
import com.progresssoft.docaccess.security.UserContextHolder;
import com.progresssoft.docaccess.service.PermissionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DocumentService Tests")
class DocumentServiceImplTest {

    @Mock private DocumentRepository documentRepository;
    @Mock private PermissionService permissionService;
    @Mock private DocumentMapper documentMapper;

    @InjectMocks
    private DocumentServiceImpl documentService;

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

    private DocumentResponse buildDocumentResponse() {
        return new DocumentResponse(
                documentId,
                "Test Document",
                "Content",
                "pdf",
                Collections.emptyList()
        );
    }

    @Nested
    @DisplayName("createDocument()")
    class CreateDocument {

        @Test
        @DisplayName("creates document successfully when user is admin")
        void createsDocument_whenAdmin() {
            UserContextHolder.setCurrentUser("admin");
            CreateDocumentRequest request = new CreateDocumentRequest(
                    "Test", "Content", "pdf", null
            );
            Document document = buildDocument();
            DocumentResponse response = buildDocumentResponse();

            when(permissionService.isAdmin()).thenReturn(true);
            when(documentMapper.toEntity(request, "admin")).thenReturn(document);
            when(documentRepository.save(document)).thenReturn(document);
            when(documentMapper.toResponse(document)).thenReturn(response);

            DocumentResponse result = documentService.createDocument(request);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(documentId);
            assertThat(result.name()).isEqualTo("Test Document");
            verify(documentRepository).save(document);
        }

        @Test
        @DisplayName("throws AccessDeniedException when user is not admin")
        void throwsAccessDeniedException_whenNotAdmin() {
            UserContextHolder.setCurrentUser("user1");
            CreateDocumentRequest request = new CreateDocumentRequest(
                    "Test", "Content", "pdf", null
            );

            when(permissionService.isAdmin()).thenReturn(false);

            assertThatThrownBy(() -> documentService.createDocument(request))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("Only admin can create documents");

            verifyNoInteractions(documentRepository);
            verifyNoInteractions(documentMapper);
        }

        @Test
        @DisplayName("never calls repository when user is not admin")
        void neverCallsRepository_whenNotAdmin() {
            UserContextHolder.setCurrentUser("user1");
            CreateDocumentRequest request = new CreateDocumentRequest(
                    "Test", "Content", "pdf", null
            );

            when(permissionService.isAdmin()).thenReturn(false);

            assertThatThrownBy(() -> documentService.createDocument(request))
                    .isInstanceOf(AccessDeniedException.class);

            verify(documentRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getAllAccessibleDocuments()")
    class GetAllAccessibleDocuments {

        @Test
        @DisplayName("returns all documents when user is admin")
        void returnsAllDocuments_whenAdmin() {
            UserContextHolder.setCurrentUser("admin");
            List<Document> documents = List.of(buildDocument());
            List<DocumentResponse> responses = List.of(buildDocumentResponse());

            when(permissionService.isAdmin()).thenReturn(true);
            when(documentRepository.findAll()).thenReturn(documents);
            when(documentMapper.toResponseList(documents)).thenReturn(responses);

            List<DocumentResponse> result = documentService.getAllAccessibleDocuments();

            assertThat(result).hasSize(1);
            verify(documentRepository).findAll();
            verify(documentRepository, never()).findAllWithAccessList();
        }

        @Test
        @DisplayName("returns only accessible documents when user is not admin")
        void returnsAccessibleDocuments_whenNotAdmin() {
            UserContextHolder.setCurrentUser("user1");
            List<Document> documents = List.of(buildDocument());
            List<DocumentResponse> responses = List.of(buildDocumentResponse());

            when(permissionService.isAdmin()).thenReturn(false);
            when(documentRepository.findAllWithAccessList()).thenReturn(documents);
            when(documentMapper.toResponseList(documents)).thenReturn(responses);

            List<DocumentResponse> result = documentService.getAllAccessibleDocuments();

            assertThat(result).hasSize(1);
            verify(documentRepository).findAllWithAccessList();
            verify(documentRepository, never()).findAll();
        }

        @Test
        @DisplayName("returns empty list when user has no accessible documents")
        void returnsEmptyList_whenNoAccessibleDocuments() {
            UserContextHolder.setCurrentUser("user1");

            when(permissionService.isAdmin()).thenReturn(false);
            when(documentRepository.findAllWithAccessList()).thenReturn(Collections.emptyList());
            when(documentMapper.toResponseList(Collections.emptyList())).thenReturn(Collections.emptyList());

            List<DocumentResponse> result = documentService.getAllAccessibleDocuments();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("admin never calls findAllWithAccessList")
        void adminNeverCallsFindAllWithAccessList() {
            UserContextHolder.setCurrentUser("admin");

            when(permissionService.isAdmin()).thenReturn(true);
            when(documentRepository.findAll()).thenReturn(Collections.emptyList());
            when(documentMapper.toResponseList(any())).thenReturn(Collections.emptyList());

            documentService.getAllAccessibleDocuments();

            verify(documentRepository, never()).findAllWithAccessList();
        }
    }

    @Nested
    @DisplayName("getDocumentById()")
    class GetDocumentById {

        @Test
        @DisplayName("returns document when user has READ permission")
        void returnsDocument_whenUserHasReadPermission() {
            UserContextHolder.setCurrentUser("user1");
            Document document = buildDocument();
            DocumentResponse response = buildDocumentResponse();

            when(documentRepository.findById(documentId)).thenReturn(Optional.of(document));
            when(permissionService.hasPermission(documentId, Permission.READ)).thenReturn(true);
            when(documentMapper.toResponse(document)).thenReturn(response);

            DocumentResponse result = documentService.getDocumentById(documentId);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(documentId);
        }

        @Test
        @DisplayName("returns document when user is admin")
        void returnsDocument_whenAdmin() {
            UserContextHolder.setCurrentUser("admin");
            Document document = buildDocument();
            DocumentResponse response = buildDocumentResponse();

            when(documentRepository.findById(documentId)).thenReturn(Optional.of(document));
            when(permissionService.hasPermission(documentId, Permission.READ)).thenReturn(true);
            when(documentMapper.toResponse(document)).thenReturn(response);

            DocumentResponse result = documentService.getDocumentById(documentId);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("throws DocumentNotFoundException when document does not exist")
        void throwsDocumentNotFoundException_whenDocumentNotFound() {
            when(documentRepository.findById(documentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> documentService.getDocumentById(documentId))
                    .isInstanceOf(DocumentNotFoundException.class)
                    .hasMessageContaining(documentId.toString());

            verifyNoInteractions(permissionService);
        }

        @Test
        @DisplayName("throws AccessDeniedException when user has no READ permission")
        void throwsAccessDeniedException_whenNoReadPermission() {
            UserContextHolder.setCurrentUser("user1");
            Document document = buildDocument();

            when(documentRepository.findById(documentId)).thenReturn(Optional.of(document));
            when(permissionService.hasPermission(documentId, Permission.READ)).thenReturn(false);

            assertThatThrownBy(() -> documentService.getDocumentById(documentId))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("You don't have READ permission on this document");

            verify(documentMapper, never()).toResponse(any());
        }

        @Test
        @DisplayName("never maps response when permission check fails")
        void neverMapsResponse_whenPermissionFails() {
            UserContextHolder.setCurrentUser("user1");
            Document document = buildDocument();

            when(documentRepository.findById(documentId)).thenReturn(Optional.of(document));
            when(permissionService.hasPermission(documentId, Permission.READ)).thenReturn(false);

            assertThatThrownBy(() -> documentService.getDocumentById(documentId))
                    .isInstanceOf(AccessDeniedException.class);

            verify(documentMapper, never()).toResponse(any());
        }
    }

    @Nested
    @DisplayName("deleteDocument()")
    class DeleteDocument {

        @Test
        @DisplayName("deletes document when user has DELETE permission")
        void deletesDocument_whenUserHasDeletePermission() {
            UserContextHolder.setCurrentUser("user2");
            Document document = buildDocument();

            when(documentRepository.findById(documentId)).thenReturn(Optional.of(document));
            when(permissionService.hasPermission(documentId, Permission.DELETE)).thenReturn(true);

            documentService.deleteDocument(documentId);

            verify(documentRepository).deleteById(documentId);
        }

        @Test
        @DisplayName("deletes document when user is admin")
        void deletesDocument_whenAdmin() {
            UserContextHolder.setCurrentUser("admin");
            Document document = buildDocument();

            when(documentRepository.findById(documentId)).thenReturn(Optional.of(document));
            when(permissionService.hasPermission(documentId, Permission.DELETE)).thenReturn(true);

            documentService.deleteDocument(documentId);

            verify(documentRepository).deleteById(documentId);
        }

        @Test
        @DisplayName("throws DocumentNotFoundException when document does not exist")
        void throwsDocumentNotFoundException_whenDocumentNotFound() {
            when(documentRepository.findById(documentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> documentService.deleteDocument(documentId))
                    .isInstanceOf(DocumentNotFoundException.class)
                    .hasMessageContaining(documentId.toString());

            verify(documentRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("throws AccessDeniedException when user has no DELETE permission")
        void throwsAccessDeniedException_whenNoDeletePermission() {
            UserContextHolder.setCurrentUser("user1");
            Document document = buildDocument();

            when(documentRepository.findById(documentId)).thenReturn(Optional.of(document));
            when(permissionService.hasPermission(documentId, Permission.DELETE)).thenReturn(false);

            assertThatThrownBy(() -> documentService.deleteDocument(documentId))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("You don't have DELETE permission on this document");

            verify(documentRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("never deletes when permission check fails")
        void neverDeletes_whenPermissionFails() {
            UserContextHolder.setCurrentUser("user1");
            Document document = buildDocument();

            when(documentRepository.findById(documentId)).thenReturn(Optional.of(document));
            when(permissionService.hasPermission(documentId, Permission.DELETE)).thenReturn(false);

            assertThatThrownBy(() -> documentService.deleteDocument(documentId))
                    .isInstanceOf(AccessDeniedException.class);

            verify(documentRepository, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("batchAccessCheck()")
    class BatchAccessCheck {

        @Test
        @DisplayName("returns all IDs when user is admin")
        void returnsAllIds_whenAdmin() {
            UserContextHolder.setCurrentUser("admin");
            List<UUID> ids = List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
            BatchAccessRequest request = new BatchAccessRequest(Permission.READ, ids);

            when(permissionService.isAdmin()).thenReturn(true);

            BatchAccessResponse result = documentService.batchAccessCheck(request);

            assertThat(result.accessibleIds()).containsExactlyElementsOf(ids);
            assertThat(result.accessibleIds()).hasSize(3);
            verifyNoInteractions(documentRepository);
        }

        @Test
        @DisplayName("returns only accessible IDs when user is not admin")
        void returnsOnlyAccessibleIds_whenNotAdmin() {
            UserContextHolder.setCurrentUser("user1");
            UUID accessibleId = UUID.randomUUID();
            UUID notAccessibleId = UUID.randomUUID();
            List<UUID> requestedIds = List.of(accessibleId, notAccessibleId);
            BatchAccessRequest request = new BatchAccessRequest(Permission.READ, requestedIds);

            when(permissionService.isAdmin()).thenReturn(false);
            when(documentRepository.findAllByIdInAndAccessListUsernameAndAccessListPermission(
                    requestedIds, "user1", Permission.READ
            )).thenReturn(List.of(accessibleId));

            BatchAccessResponse result = documentService.batchAccessCheck(request);

            assertThat(result.accessibleIds()).containsExactly(accessibleId);
            assertThat(result.accessibleIds()).doesNotContain(notAccessibleId);
        }

        @Test
        @DisplayName("returns empty list when user has no accessible documents")
        void returnsEmptyList_whenNoAccessibleDocuments() {
            UserContextHolder.setCurrentUser("user3");
            List<UUID> ids = List.of(UUID.randomUUID(), UUID.randomUUID());
            BatchAccessRequest request = new BatchAccessRequest(Permission.READ, ids);

            when(permissionService.isAdmin()).thenReturn(false);
            when(documentRepository.findAllByIdInAndAccessListUsernameAndAccessListPermission(
                    ids, "user3", Permission.READ
            )).thenReturn(Collections.emptyList());

            BatchAccessResponse result = documentService.batchAccessCheck(request);

            assertThat(result.accessibleIds()).isEmpty();
        }

        @Test
        @DisplayName("admin never calls repository for batch check")
        void adminNeverCallsRepository() {
            UserContextHolder.setCurrentUser("admin");
            List<UUID> ids = List.of(UUID.randomUUID());
            BatchAccessRequest request = new BatchAccessRequest(Permission.DELETE, ids);

            when(permissionService.isAdmin()).thenReturn(true);

            documentService.batchAccessCheck(request);

            verify(documentRepository, never())
                    .findAllByIdInAndAccessListUsernameAndAccessListPermission(any(), any(), any());
        }

        @Test
        @DisplayName("checks correct permission type in batch check")
        void checksCorrectPermissionType() {
            UserContextHolder.setCurrentUser("user2");
            List<UUID> ids = List.of(documentId);
            BatchAccessRequest request = new BatchAccessRequest(Permission.DELETE, ids);

            when(permissionService.isAdmin()).thenReturn(false);
            when(documentRepository.findAllByIdInAndAccessListUsernameAndAccessListPermission(
                    ids, "user2", Permission.DELETE
            )).thenReturn(List.of(documentId));

            BatchAccessResponse result = documentService.batchAccessCheck(request);

            assertThat(result.accessibleIds()).containsExactly(documentId);
            verify(documentRepository).findAllByIdInAndAccessListUsernameAndAccessListPermission(
                    ids, "user2", Permission.DELETE
            );
        }
    }
}