package com.progresssoft.docaccess.entity;


import com.progresssoft.docaccess.enums.Permission;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "document_accesses",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"document_id", "username", "permission"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Username is required")
    @Column(nullable = false)
    private String username;


    @NotNull(message = "Permission is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Permission permission;

    @NotNull(message = "Document is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Document document;

}
