package com.smartstay.console.repositories;

import com.smartstay.console.dao.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersRepository extends JpaRepository<Users, String> {
    Users findByEmailId(String emailId);
}
