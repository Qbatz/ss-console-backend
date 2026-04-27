package com.smartstay.console.repositories;

import com.smartstay.console.dao.TableColumns;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface TableColumnsRepository extends JpaRepository<TableColumns, Long> {

    List<TableColumns> findAllByUserIdIn(Set<String> userIds);

    List<TableColumns> findAllByHostelId(String hostelId);
}
