package com.smartstay.console.repositories;

import com.smartstay.console.dao.UserHostel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface UserHostelRepository extends JpaRepository<UserHostel, Integer> {

    List<UserHostel> findAllByHostelId(String hostelId);

    List<UserHostel> findAllByParentId(String parentId);

    boolean existsByHostelIdAndUserId(String hostelId, String userId);

    List<UserHostel> findAllByParentIdIn(Set<String> parentIds);
}
