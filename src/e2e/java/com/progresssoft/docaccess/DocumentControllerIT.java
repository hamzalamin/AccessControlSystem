package com.progresssoft.docaccess;

import com.progresssoft.docaccess.repository.DocumentAccessRepository;
import com.progresssoft.docaccess.repository.DocumentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Document Controller Integration Tests")
public class DocumentControllerIT extends AbstractIT {

    @Autowired private MockMvcTester mvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private DocumentRepository documentRepository;
    @Autowired private DocumentAccessRepository documentAccessRepository;

    private String documentId;

    @BeforeEach
    void setUp() throws Exception {
        String request = """
            {
              "name": "Test Document",
              "content": "Test Content",
              "fileType": "pdf",
              "accessibleUsers": [
                { "username": "user1", "permission": "READ" },
                { "username": "user2", "permission": "DELETE" }
              ]
            }
            """;

        var response = mvc.post()
                .uri("/documents")
                .header("X-User", "admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .exchange();

        documentId = objectMapper.readTree(response.getResponse().getContentAsString())
                .get("id")
                .asText();
    }

    @AfterEach
    void tearDown() {
        documentAccessRepository.deleteAll();
        documentRepository.deleteAll();
    }

    @Nested
    @DisplayName("POST /documents")
    class CreateDocument {

        @Test
        void givenInvalidRequest_whenCreateDocument_thenReturnsBadRequest() {
            String invalidRequest = """
                    {
                      "content": "This is the content is invalid",
                      "fileType": "csv1",
                      "accessibleUsers": [
                        { "username": "Hamza", "permission": "READ" },
                        { "username": "Ayman", "permission": "DELETE" }
                      ]
                    }
                    """;

            mvc.post()
                    .uri("/documents")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidRequest)
                    .header("X-User", "admin")
                    .assertThat()
                    .hasStatus(400)
                    .bodyJson()
                    .hasPathSatisfying("$.details.name", name -> name.assertThat().isEqualTo("must not be blank"))
                    .hasPathSatisfying("$.message", msg -> msg.assertThat().isEqualTo("Validation failed"));
        }

        @Test
        void givenValidRequestAndInvalidUser_whenCreateDocument_thenReturnsForbidden() {
            String request = """
                    {
                      "name": "Test Document 1",
                      "content": "This is the content is invalid",
                      "fileType": "csv1",
                      "accessibleUsers": [
                        { "username": "Hamza", "permission": "READ" },
                        { "username": "Ayman", "permission": "DELETE" }
                      ]
                    }
                    """;

            mvc.post()
                    .uri("/documents")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request)
                    .header("X-User", "notAdmin")
                    .assertThat()
                    .hasStatus(403)
                    .bodyJson()
                    .hasPathSatisfying("$.message", msg -> msg.assertThat().isEqualTo("Only admin can create documents"));
        }

        @Test
        void givenValidRequest_whenCreateDocument_thenReturnsHappyPath() {
            String validRequest = """
                    {
                      "name": "Test Document 1",
                      "content": "This is the content is invalid",
                      "fileType": "csv1",
                      "accessibleUsers": [
                        { "username": "Hamza", "permission": "READ" },
                        { "username": "Ayman", "permission": "DELETE" }
                      ]
                    }
                    """;

            mvc.post()
                    .uri("/documents")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validRequest)
                    .header("X-User", "admin")
                    .assertThat()
                    .hasStatus(201)
                    .bodyJson()
                    .hasPathSatisfying("$.content", content -> content.assertThat().isEqualTo("This is the content is invalid"));
        }
    }

    @Nested
    @DisplayName("GET /documents")
    class GetAllDocuments {

        @Test
        void givenAdminUser_whenGetAllDocuments_thenReturnsAll() {
            mvc.get()
                    .uri("/documents")
                    .header("X-User", "admin")
                    .assertThat()
                    .hasStatus(200)
                    .bodyJson()
                    .hasPathSatisfying("$.length()", len -> len.assertThat().isEqualTo(1));
        }

        @Test
        void givenUserWithReadPermission_whenGetAllDocuments_thenReturnsOnlyAccessible() {
            mvc.get()
                    .uri("/documents")
                    .header("X-User", "user1")
                    .assertThat()
                    .hasStatus(200)
                    .bodyJson()
                    .hasPathSatisfying("$.length()", len -> len.assertThat().isEqualTo(1));
        }

        @Test
        void givenUserWithNoPermission_whenGetAllDocuments_thenReturnsEmptyList() {
            mvc.get()
                    .uri("/documents")
                    .header("X-User", "user3")
                    .assertThat()
                    .hasStatus(200)
                    .bodyJson()
                    .hasPathSatisfying("$.length()", len -> len.assertThat().isEqualTo(0));
        }
    }

    @Nested
    @DisplayName("GET /documents/{id}")
    class GetDocumentById {

        @Test
        void givenAdminUser_whenGetDocumentById_thenReturnsDocument() {
            mvc.get()
                    .uri("/documents/{id}", documentId)
                    .header("X-User", "admin")
                    .assertThat()
                    .hasStatus(200)
                    .bodyJson()
                    .hasPathSatisfying("$.id", id -> id.assertThat().isEqualTo(documentId));
        }

        @Test
        void givenUserWithNoPermission_whenGetDocumentById_thenReturnsForbidden() {
            mvc.get()
                    .uri("/documents/{id}", documentId)
                    .header("X-User", "user3")
                    .assertThat()
                    .hasStatus(403)
                    .bodyJson()
                    .hasPathSatisfying("$.message", msg ->
                            msg.assertThat().isEqualTo("You don't have READ permission on this document"));
        }

        @Test
        void givenNonExistingId_whenGetDocumentById_thenReturnsNotFound() {
            mvc.get()
                    .uri("/documents/{id}", UUID.randomUUID())
                    .header("X-User", "admin")
                    .assertThat()
                    .hasStatus(404);
        }
    }

    @Nested
    @DisplayName("DELETE /documents/{id}")
    class DeleteDocument {

        @Test
        void givenUserWithDeletePermission_whenDeleteDocument_thenReturnsNoContent() {
            mvc.delete()
                    .uri("/documents/{id}", documentId)
                    .header("X-User", "user2")
                    .assertThat()
                    .hasStatus(204);
        }

        @Test
        void givenUserWithNoPermission_whenDeleteDocument_thenReturnsForbidden() {
            mvc.delete()
                    .uri("/documents/{id}", documentId)
                    .header("X-User", "user1")
                    .assertThat()
                    .hasStatus(403)
                    .bodyJson()
                    .hasPathSatisfying("$.message", msg ->
                            msg.assertThat().isEqualTo("You don't have DELETE permission on this document"));
        }

        @Test
        void givenNonExistingId_whenDeleteDocument_thenReturnsNotFound() {
            mvc.delete()
                    .uri("/documents/{id}", UUID.randomUUID())
                    .header("X-User", "admin")
                    .assertThat()
                    .hasStatus(404);
        }
    }

    @Nested
    @DisplayName("POST /documents/{id}/grant")
    class GrantPermission {

        @Test
        void givenAdminUser_whenGrantPermission_thenReturnsOk() {
            String request = """
                    { "username": "user3", "permission": "READ" }
                    """;

            mvc.post()
                    .uri("/documents/{id}/grant", documentId)
                    .header("X-User", "admin")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request)
                    .assertThat()
                    .hasStatus(200);
        }

        @Test
        void givenUserWithOnlyReadPermission_whenGrantPermission_thenReturnsForbidden() {
            String request = """
                    { "username": "user3", "permission": "READ" }
                    """;

            mvc.post()
                    .uri("/documents/{id}/grant", documentId)
                    .header("X-User", "user1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request)
                    .assertThat()
                    .hasStatus(403)
                    .bodyJson()
                    .hasPathSatisfying("$.message", msg ->
                            msg.assertThat().isEqualTo("You don't have permission to grant access"));
        }

        @Test
        void givenNonExistingDocument_whenGrantPermission_thenReturnsNotFound() {
            String request = """
                    { "username": "user3", "permission": "READ" }
                    """;

            mvc.post()
                    .uri("/documents/{id}/grant", UUID.randomUUID())
                    .header("X-User", "admin")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request)
                    .assertThat()
                    .hasStatus(404);
        }
    }

    @Nested
    @DisplayName("POST /documents/access-check")
    class BatchAccessCheck {

        @Test
        void givenAdminUser_whenBatchAccessCheck_thenReturnsAllIds() {
            String request = """
                    {
                      "permission": "READ",
                      "documentIds": ["%s", "%s"]
                    }
                    """.formatted(documentId, UUID.randomUUID());

            mvc.post()
                    .uri("/documents/access-check")
                    .header("X-User", "admin")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request)
                    .assertThat()
                    .hasStatus(200)
                    .bodyJson()
                    .hasPathSatisfying("$.accessibleIds.length()", len -> len.assertThat().isEqualTo(2));
        }

        @Test
        void givenUserWithReadPermission_whenBatchAccessCheck_thenReturnsOnlyAccessibleIds() {
            String request = """
                    {
                      "permission": "READ",
                      "documentIds": ["%s", "%s"]
                    }
                    """.formatted(documentId, UUID.randomUUID());

            mvc.post()
                    .uri("/documents/access-check")
                    .header("X-User", "user1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request)
                    .assertThat()
                    .hasStatus(200)
                    .bodyJson()
                    .hasPathSatisfying("$.accessibleIds.length()", len -> len.assertThat().isEqualTo(1))
                    .hasPathSatisfying("$.accessibleIds[0]", id -> id.assertThat().isEqualTo(documentId));
        }

        @Test
        void givenEmptyDocumentIds_whenBatchAccessCheck_thenReturnsBadRequest() {
            String request = """
                    { "permission": "READ", "documentIds": [] }
                    """;

            mvc.post()
                    .uri("/documents/access-check")
                    .header("X-User", "user1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request)
                    .assertThat()
                    .hasStatus(400);
        }
    }
}