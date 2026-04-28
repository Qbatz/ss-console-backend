package com.smartstay.console.repositories;

import com.smartstay.console.dao.RolesV1;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolesRepository extends JpaRepository<RolesV1, Integer> {

    List<RolesV1> findAllByHostelId(String hostelId);
}
