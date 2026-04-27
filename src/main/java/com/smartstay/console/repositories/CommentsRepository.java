package com.smartstay.console.repositories;

import com.smartstay.console.dao.Comments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface CommentsRepository extends JpaRepository<Comments, Long> {

    List<Comments> findAllByUserIdIn(Set<String> userIds);
}
