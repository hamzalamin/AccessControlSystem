package com.progresssoft.docaccess.repository;

import com.progresssoft.docaccess.entity.Document;
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
        AND a.permission = 'READ'
    """)
    List<Document> findAllAccessibleByUsername(@Param("username") String username);

}
