package com.smartstay.console.repositories;

import com.smartstay.console.dao.UsersNotes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserNotesRepository extends JpaRepository<UsersNotes, Long> {

    List<UsersNotes> findAllByUserIdOrderByIdDesc(String userId);
}
