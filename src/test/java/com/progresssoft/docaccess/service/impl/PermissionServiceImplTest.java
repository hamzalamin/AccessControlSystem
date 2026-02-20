package com.progresssoft.docaccess.service.impl;

import com.progresssoft.docaccess.enums.Permission;
import com.progresssoft.docaccess.repository.DocumentAccessRepository;
import com.progresssoft.docaccess.security.UserContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PermissionService tests")
class PermissionServiceImplTest {

    @Mock
    private DocumentAccessRepository documentAccessRepository;
    @InjectMocks
    private PermissionServiceImpl sut;

    private final UUID documentId = UUID.randomUUID();

    @AfterEach
    void clearContext() {
        UserContextHolder.clear();
    }

    @Nested
    @DisplayName("isAdmin()")
    class IsAdmin {

        @Test
        @DisplayName("returns true when current user is admin")
        void returnsTrue_whenUserIsAdmin(){
            UserContextHolder.setCurrentUser("admin");
            assertThat(sut.isAdmin()).isTrue();
        }

        @Test
        @DisplayName("returns false when current user is not admin")
        void returnFals_whenUserIsNotAdmin() {
            UserContextHolder.setCurrentUser("notAdmin");
            assertThat(sut.isAdmin()).isFalse();
        }

    }

    @Nested
    @DisplayName("hasPermission()")
    class HasPermission {
        @Test
        @DisplayName("returns true for admin without checking repository")
        void returnsTrue_forAdmin_withoutCheckingRepository() {
            UserContextHolder.setCurrentUser("admin");
            boolean result = sut.hasPermission(documentId, Permission.READ);
            assertThat(result).isTrue();
            verifyNoInteractions(documentAccessRepository);
        }

        @Test
        @DisplayName("returns true when user has the permission")
        void returnsTrue_whenUserHasPermission() {
            UserContextHolder.setCurrentUser("user1");
            when(documentAccessRepository.existsByDocumentIdAndUsernameAndPermission(
                    documentId, "user1", Permission.READ
            )).thenReturn(true);

            boolean result = sut.hasPermission(documentId, Permission.READ);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("returns false when user does not have the permission")
        void returnsFalse_whenUserDoesNotHavePermission() {
            UserContextHolder.setCurrentUser("user1");
            when(documentAccessRepository.existsByDocumentIdAndUsernameAndPermission(
                    documentId, "user1", Permission.DELETE
            )).thenReturn(false);

            boolean result = sut.hasPermission(documentId, Permission.DELETE);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("checks all permission types correctly")
        void checksAllPermissionTypes() {
            UserContextHolder.setCurrentUser("user1");

            when(documentAccessRepository.existsByDocumentIdAndUsernameAndPermission(
                    documentId, "user1", Permission.READ)).thenReturn(true);
            when(documentAccessRepository.existsByDocumentIdAndUsernameAndPermission(
                    documentId, "user1", Permission.WRITE)).thenReturn(true);
            when(documentAccessRepository.existsByDocumentIdAndUsernameAndPermission(
                    documentId, "user1", Permission.DELETE)).thenReturn(false);

            assertThat(sut.hasPermission(documentId, Permission.READ)).isTrue();
            assertThat(sut.hasPermission(documentId, Permission.WRITE)).isTrue();
            assertThat(sut.hasPermission(documentId, Permission.DELETE)).isFalse();
        }

    }

    @Nested
    @DisplayName("canGrant()")
    class CanGrant {

        @Test
        @DisplayName("returns true for admin without checking repository")
        void returnsTrue_forAdmin() {
            UserContextHolder.setCurrentUser("admin");

            boolean result = sut.canGrant(documentId);

            assertThat(result).isTrue();
            verifyNoInteractions(documentAccessRepository);
        }

        @Test
        @DisplayName("returns true when user has WRITE permission")
        void returnsTrue_whenUserHasWritePermission() {
            UserContextHolder.setCurrentUser("user1");
            when(documentAccessRepository.existsByDocumentIdAndUsernameAndPermission(
                    documentId, "user1", Permission.WRITE
            )).thenReturn(true);

            boolean result = sut.canGrant(documentId);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("returns false when user has only READ permission")
        void returnsFalse_whenUserHasOnlyReadPermission() {
            UserContextHolder.setCurrentUser("user1");
            when(documentAccessRepository.existsByDocumentIdAndUsernameAndPermission(
                    documentId, "user1", Permission.WRITE
            )).thenReturn(false);

            boolean result = sut.canGrant(documentId);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("returns false when user has no permissions at all")
        void returnsFalse_whenUserHasNoPermissions() {
            UserContextHolder.setCurrentUser("user3");
            when(documentAccessRepository.existsByDocumentIdAndUsernameAndPermission(
                    documentId, "user3", Permission.WRITE
            )).thenReturn(false);

            boolean result = sut.canGrant(documentId);

            assertThat(result).isFalse();
        }

    }



}