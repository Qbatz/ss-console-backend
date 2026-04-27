package com.smartstay.console.repositories;

import com.smartstay.console.dao.DemoRequestComments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DemoRequestCommentsRepository extends JpaRepository<DemoRequestComments, Long> {
}
