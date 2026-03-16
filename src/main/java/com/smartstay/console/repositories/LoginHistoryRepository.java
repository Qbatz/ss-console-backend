package com.smartstay.console.repositories;

import com.smartstay.console.dao.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {
    @Query("""
            SELECT lh FROM LoginHistory lh WHERE lh.parentId in (:parentId)
            AND lh.loginAt=(SELECT MAX(lh2.loginAt) from LoginHistory lh2 where lh2.parentId=lh.parentId)
            """)
    List<LoginHistory> loginHistoryByParentId(@Param("parentId") List<String> parentId);
}
