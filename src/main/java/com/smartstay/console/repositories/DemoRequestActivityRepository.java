package com.smartstay.console.repositories;

import com.smartstay.console.dao.DemoRequestActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface DemoRequestActivityRepository extends JpaRepository<DemoRequestActivity, Long> {

    List<DemoRequestActivity> findAllByRequestIdInOrderByActivityIdDesc(Set<Long> demoRequestIds);

    List<DemoRequestActivity> findAllByRequestIdOrderByActivityIdDesc(Long requestId);
}
