package com.smartstay.console.repositories;

import com.smartstay.console.dao.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsersRepository extends JpaRepository<Users, String> {
    Users findByEmailId(String emailId);
    @Query("""
            SELECT usr FROM Users usr WHERE usr.parentId IN (:parentId) AND usr.roleId=1
            """)
    List<Users> findOwners(List<String> parentId);

    @Query(value = """
            SELECT DISTINCT usr
            FROM Users usr
            LEFT JOIN hostelv1 h ON h.parentId = usr.parentId
            WHERE usr.roleId = 1
              AND (
                    :name IS NULL OR :name = '' OR
                    LOWER(usr.firstName) LIKE LOWER(CONCAT('%', :name, '%'))
                    OR LOWER(usr.lastName) LIKE LOWER(CONCAT('%', :name, '%'))
                    OR LOWER(h.hostelName) LIKE LOWER(CONCAT('%', :name, '%'))
                  )
            """,
            countQuery = """
            SELECT COUNT(DISTINCT usr)
            FROM Users usr
            LEFT JOIN hostelv1 h ON h.parentId = usr.parentId
            WHERE usr.roleId = 1
              AND (
                    :name IS NULL OR :name = '' OR
                    LOWER(usr.firstName) LIKE LOWER(CONCAT('%', :name, '%'))
                    OR LOWER(usr.lastName) LIKE LOWER(CONCAT('%', :name, '%'))
                    OR LOWER(h.hostelName) LIKE LOWER(CONCAT('%', :name, '%'))
                  )
            """)
    Page<Users> findAllOwners(@Param("name") String name, Pageable pageable);

    @Query(value = """
            SELECT DISTINCT usr
            FROM Users usr
            LEFT JOIN hostelv1 h ON h.parentId = usr.parentId
            LEFT JOIN UserActivities ua ON ua.parentId = usr.parentId
            WHERE usr.roleId = 1
              AND (
                    :name IS NULL OR :name = '' OR
                    LOWER(usr.firstName) LIKE LOWER(CONCAT('%', :name, '%'))
                    OR LOWER(usr.lastName) LIKE LOWER(CONCAT('%', :name, '%'))
                    OR LOWER(h.hostelName) LIKE LOWER(CONCAT('%', :name, '%'))
                  )
            ORDER BY (
               SELECT MAX(ua2.createdAt)
               FROM UserActivities ua2
               WHERE ua2.parentId = usr.parentId
            ) DESC
            """,
            countQuery = """
            SELECT COUNT(DISTINCT usr)
            FROM Users usr
            LEFT JOIN hostelv1 h ON h.parentId = usr.parentId
            WHERE usr.roleId = 1
              AND (
                    :name IS NULL OR :name = '' OR
                    LOWER(usr.firstName) LIKE LOWER(CONCAT('%', :name, '%'))
                    OR LOWER(usr.lastName) LIKE LOWER(CONCAT('%', :name, '%'))
                    OR LOWER(h.hostelName) LIKE LOWER(CONCAT('%', :name, '%'))
                  )
            """)
    Page<Users> findAllOwnersOrderByLatestActivityDesc(@Param("name") String name, Pageable pageable);

    @Query(value = """
            SELECT DISTINCT usr
            FROM Users usr
            LEFT JOIN hostelv1 h ON h.parentId = usr.parentId
            LEFT JOIN UserActivities ua ON ua.parentId = usr.parentId
            WHERE usr.roleId = 1
              AND (
                    :name IS NULL OR :name = '' OR
                    LOWER(usr.firstName) LIKE LOWER(CONCAT('%', :name, '%'))
                    OR LOWER(usr.lastName) LIKE LOWER(CONCAT('%', :name, '%'))
                    OR LOWER(h.hostelName) LIKE LOWER(CONCAT('%', :name, '%'))
                  )
            ORDER BY (
               SELECT MAX(ua2.createdAt)
               FROM UserActivities ua2
               WHERE ua2.parentId = usr.parentId
            ) ASC
            """,
            countQuery = """
            SELECT COUNT(DISTINCT usr)
            FROM Users usr
            LEFT JOIN hostelv1 h ON h.parentId = usr.parentId
            WHERE usr.roleId = 1
              AND (
                    :name IS NULL OR :name = '' OR
                    LOWER(usr.firstName) LIKE LOWER(CONCAT('%', :name, '%'))
                    OR LOWER(usr.lastName) LIKE LOWER(CONCAT('%', :name, '%'))
                    OR LOWER(h.hostelName) LIKE LOWER(CONCAT('%', :name, '%'))
                  )
            """)
    Page<Users> findAllOwnersOrderByLatestActivityAsc(@Param("name") String name, Pageable pageable);

}
