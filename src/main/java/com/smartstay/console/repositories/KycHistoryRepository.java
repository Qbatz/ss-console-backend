package com.smartstay.console.repositories;

import com.smartstay.console.dao.KycHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface KycHistoryRepository extends JpaRepository<KycHistory, Long> {

    KycHistory findTopByHostelIdOrderByHistoryIdDesc(String hostelId);

    @Query("""
            SELECT kh
            FROM KycHistory kh
            INNER JOIN (
                SELECT kh2.hostelId AS hostelId, MAX(kh2.historyId) AS historyId
                FROM KycHistory kh2
                WHERE kh2.hostelId IN :hostelIds
                GROUP BY kh2.hostelId
            ) latest
                ON latest.hostelId = kh.hostelId
               AND latest.historyId = kh.historyId
            WHERE kh.hostelId IN :hostelIds
            """)
    List<KycHistory> findLatestByHostelIds(@Param("hostelIds") Set<String> hostelIds);
}
