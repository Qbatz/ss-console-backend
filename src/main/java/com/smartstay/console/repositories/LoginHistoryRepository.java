package com.smartstay.console.repositories;

import com.smartstay.console.dao.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {

    @Query(value = """
                    SELECT lh.*
                    FROM login_history lh
                    JOIN (
                        SELECT parent_id, MAX(login_at) AS max_login_at
                        FROM login_history
                        WHERE parent_id IN (:parentId)
                        GROUP BY parent_id
                    ) latest
                    ON lh.parent_id = latest.parent_id
                    AND lh.login_at = latest.max_login_at
            """, nativeQuery = true)
    List<LoginHistory> loginHistoryByParentId(@Param("parentId") List<String> parentId);
}
