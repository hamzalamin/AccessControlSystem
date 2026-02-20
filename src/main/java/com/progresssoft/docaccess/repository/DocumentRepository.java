package com.progresssoft.docaccess.repository;

import com.progresssoft.docaccess.entity.Document;
import com.progresssoft.docaccess.enums.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {

    @Query("""
        SELECT DISTINCT d FROM Document d
        JOIN d.accessList a
        WHERE a.username = :username
        AND a.permission = :permission
    """)
    List<Document> findAllAccessibleByUsername(
            @Param("username") String username,
            @Param("permission") Permission permission
    );

    @Query("""
        SELECT DISTINCT d.id FROM Document d
        JOIN d.accessList a
        WHERE d.id IN :ids
        AND a.username = :username
        AND a.permission = :permission
    """)
    List<UUID> findAllByIdInAndAccessListUsernameAndAccessListPermission(
            @Param("ids") List<UUID> ids,
            @Param("username") String username,
            @Param("permission") Permission permission
    );

    @Query("""
    SELECT DISTINCT d FROM Document d
    LEFT JOIN FETCH d.accessList
""")
    List<Document> findAllWithAccessList();
}
