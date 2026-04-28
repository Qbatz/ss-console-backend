package com.smartstay.console.repositories;

import com.smartstay.console.dao.ComplaintTypeV1;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintTypeRepository extends JpaRepository<ComplaintTypeV1, Integer> {

    List<ComplaintTypeV1> findAllByHostelId(String hostelId);
}
