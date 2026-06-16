package com.smartstay.console.repositories;

import com.smartstay.console.dao.HostelNotes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HostelNotesRepository extends JpaRepository<HostelNotes,Long> {

    List<HostelNotes> findAllByHostelIdOrderByIdDesc(String hostelId);
}
