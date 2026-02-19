package com.progresssoft.docaccess.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "name is required")
    @Column(nullable = false)
    private String name;


    @NotBlank(message = "content is required")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;


    @NotBlank(message = "file type is required")
    @Column(nullable = false)
    private String fileType;


    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DocumentAccess> accessList = new ArrayList<>();

    @NotBlank
    @Column(nullable = false, updatable = false)
    private String createdBy;

}
