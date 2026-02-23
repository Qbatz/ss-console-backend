package com.smartstay.console.repositories;

import com.smartstay.console.dao.BookingsV1;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingsRepository extends JpaRepository<BookingsV1, String> {
    List<BookingsV1> findAllByHostelId(String hostelId);
}
