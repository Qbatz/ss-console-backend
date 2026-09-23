package com.smartstay.console.repositories;

import com.smartstay.console.dao.HostelFollowUp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HostelFollowUpRepository extends JpaRepository<HostelFollowUp, Long> {

    HostelFollowUp findTopByHostelIdOrderByFollowUpIdDesc(String hostelId);

    List<HostelFollowUp> findAllByHostelIdOrderByFollowUpIdDesc(String hostelId);
}
