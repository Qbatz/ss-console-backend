package com.smartstay.console.repositories;

import com.smartstay.console.dao.TableColumns;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface TableColumnsRepository extends JpaRepository<TableColumns, Long> {

    List<TableColumns> findAllByUserIdIn(Set<String> userIds);

    List<TableColumns> findAllByHostelId(String hostelId);

    @Query("""
                SELECT DISTINCT t.hostelId
                FROM TableColumns t
                WHERE (:hostelIds IS NULL OR t.hostelId IN :hostelIds)
            """)
    Page<String> findDistinctHostelIds(Set<String> hostelIds, Pageable pageable);

    List<TableColumns> findAllByHostelIdIn(Set<String> hostelIds);
}
