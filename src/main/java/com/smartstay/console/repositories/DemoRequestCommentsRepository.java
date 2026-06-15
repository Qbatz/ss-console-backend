package com.smartstay.console.repositories;

import com.smartstay.console.dao.DemoRequestComments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface DemoRequestCommentsRepository extends JpaRepository<DemoRequestComments, Long> {

    List<DemoRequestComments> findAllByRequestIdInOrderByIdDesc(Set<Long> demoRequestIds);

    List<DemoRequestComments> findAllByRequestIdOrderByIdDesc(Long requestId);
}
