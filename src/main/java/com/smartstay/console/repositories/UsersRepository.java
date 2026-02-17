package com.smartstay.console.repositories;

import com.smartstay.console.dao.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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
            WHERE usr.roleId = 1
              AND (
                    :name IS NULL OR :name = '' OR
                    LOWER(usr.firstName) LIKE LOWER(CONCAT('%', :name, '%'))
                    OR LOWER(usr.lastName) LIKE LOWER(CONCAT('%', :name, '%'))
                    OR LOWER(h.hostelName) LIKE LOWER(CONCAT('%', :name, '%'))
                  )
            ORDER BY (
                SELECT MAX(ua.createdAt)
                FROM UserActivities ua
                WHERE ua.parentId = usr.parentId
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
            WHERE usr.roleId = 1
              AND (
                    :name IS NULL OR :name = '' OR
                    LOWER(usr.firstName) LIKE LOWER(CONCAT('%', :name, '%'))
                    OR LOWER(usr.lastName) LIKE LOWER(CONCAT('%', :name, '%'))
                    OR LOWER(h.hostelName) LIKE LOWER(CONCAT('%', :name, '%'))
                  )
            ORDER BY (
                SELECT MAX(ua.createdAt)
                FROM UserActivities ua
                WHERE ua.parentId = usr.parentId
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

    @Query(value = """
            SELECT DISTINCT usr
            FROM Users usr
            LEFT JOIN hostelv1 h ON h.parentId = usr.parentId
            LEFT JOIN HostelPlan hp ON hp.hostel = h
            WHERE usr.roleId = 1
              AND (
                    :name IS NULL OR :name = '' OR
                    LOWER(usr.firstName) LIKE LOWER(CONCAT('%', :name, '%'))
                    OR LOWER(usr.lastName) LIKE LOWER(CONCAT('%', :name, '%'))
                    OR LOWER(h.hostelName) LIKE LOWER(CONCAT('%', :name, '%'))
                  )
              AND (
                    (:expired = TRUE AND (
                        hp.currentPlanEndsAt IS NULL
                        OR hp.currentPlanEndsAt < :today
                    ))
                    OR
                    (:aboutToExpire = TRUE AND
                        hp.currentPlanEndsAt BETWEEN :today AND :plus10
                    )
                  )
            """,
            countQuery = """
            SELECT COUNT(DISTINCT usr)
            FROM Users usr
            LEFT JOIN hostelv1 h ON h.parentId = usr.parentId
            LEFT JOIN HostelPlan hp ON hp.hostel = h
            WHERE usr.roleId = 1
              AND (
                    :name IS NULL OR :name = '' OR
                    LOWER(usr.firstName) LIKE LOWER(CONCAT('%', :name, '%'))
                    OR LOWER(usr.lastName) LIKE LOWER(CONCAT('%', :name, '%'))
                    OR LOWER(h.hostelName) LIKE LOWER(CONCAT('%', :name, '%'))
                  )
              AND (
                    (:expired = TRUE AND (
                        hp.currentPlanEndsAt IS NULL
                        OR hp.currentPlanEndsAt < :today
                    ))
                    OR
                    (:aboutToExpire = TRUE AND
                        hp.currentPlanEndsAt BETWEEN :today AND :plus10
                    )
                  )
            """)
    Page<Users> findAllOwnersWithExpiry(@Param("name") String name,
                                        @Param("expired") boolean expired,
                                        @Param("aboutToExpire") boolean aboutToExpire,
                                        @Param("today") LocalDateTime today,
                                        @Param("plus10") LocalDateTime plus10,
                                        Pageable pageable);

    @Query(value = """
            SELECT DISTINCT usr
            FROM Users usr
            LEFT JOIN hostelv1 h ON h.parentId = usr.parentId
            LEFT JOIN HostelPlan hp ON hp.hostel = h
            WHERE usr.roleId = 1
              AND (
                    :name IS NULL OR :name = '' OR
                    LOWER(usr.firstName) LIKE LOWER(CONCAT('%', :name, '%'))
                    OR LOWER(usr.lastName) LIKE LOWER(CONCAT('%', :name, '%'))
                    OR LOWER(h.hostelName) LIKE LOWER(CONCAT('%', :name, '%'))
                  )
              AND (
                    (:expired = TRUE AND (
                        hp.currentPlanEndsAt IS NULL
                        OR hp.currentPlanEndsAt < :today
                    ))
                    OR
                    (:aboutToExpire = TRUE AND
                        hp.currentPlanEndsAt BETWEEN :today AND :plus10
                    )
                  )
            ORDER BY (
                SELECT MAX(ua.createdAt)
                FROM UserActivities ua
                WHERE ua.parentId = usr.parentId
            ) DESC
            """,
            countQuery = """
            SELECT COUNT(DISTINCT usr)
            FROM Users usr
            LEFT JOIN hostelv1 h ON h.parentId = usr.parentId
            LEFT JOIN HostelPlan hp ON hp.hostel = h
            WHERE usr.roleId = 1
              AND (
                    :name IS NULL OR :name = '' OR
                    LOWER(usr.firstName) LIKE LOWER(CONCAT('%', :name, '%'))
                    OR LOWER(usr.lastName) LIKE LOWER(CONCAT('%', :name, '%'))
                    OR LOWER(h.hostelName) LIKE LOWER(CONCAT('%', :name, '%'))
                  )
              AND (
                    (:expired = TRUE AND (
                        hp.currentPlanEndsAt IS NULL
                        OR hp.currentPlanEndsAt < :today
                    ))
                    OR
                    (:aboutToExpire = TRUE AND
                        hp.currentPlanEndsAt BETWEEN :today AND :plus10
                    )
                  )
            """)
    Page<Users> findAllOwnersWithExpiryOrderByLatestActivityDesc(@Param("name") String name,
                                                                 @Param("expired") boolean expired,
                                                                 @Param("aboutToExpire") boolean aboutToExpire,
                                                                 @Param("today") LocalDateTime today,
                                                                 @Param("plus10") LocalDateTime plus10,
                                                                 Pageable pageable);

    @Query(value = """
            SELECT DISTINCT usr
            FROM Users usr
            LEFT JOIN hostelv1 h ON h.parentId = usr.parentId
            LEFT JOIN HostelPlan hp ON hp.hostel = h
            WHERE usr.roleId = 1
              AND (
                    :name IS NULL OR :name = '' OR
                    LOWER(usr.firstName) LIKE LOWER(CONCAT('%', :name, '%'))
                    OR LOWER(usr.lastName) LIKE LOWER(CONCAT('%', :name, '%'))
                    OR LOWER(h.hostelName) LIKE LOWER(CONCAT('%', :name, '%'))
                  )
              AND (
                    (:expired = TRUE AND (
                        hp.currentPlanEndsAt IS NULL
                        OR hp.currentPlanEndsAt < :today
                    ))
                    OR
                    (:aboutToExpire = TRUE AND
                        hp.currentPlanEndsAt BETWEEN :today AND :plus10
                    )
                  )
            ORDER BY (
                SELECT MAX(ua.createdAt)
                FROM UserActivities ua
                WHERE ua.parentId = usr.parentId
            ) ASC
            """,
            countQuery = """
            SELECT COUNT(DISTINCT usr)
            FROM Users usr
            LEFT JOIN hostelv1 h ON h.parentId = usr.parentId
            LEFT JOIN HostelPlan hp ON hp.hostel = h
            WHERE usr.roleId = 1
              AND (
                    :name IS NULL OR :name = '' OR
                    LOWER(usr.firstName) LIKE LOWER(CONCAT('%', :name, '%'))
                    OR LOWER(usr.lastName) LIKE LOWER(CONCAT('%', :name, '%'))
                    OR LOWER(h.hostelName) LIKE LOWER(CONCAT('%', :name, '%'))
                  )
              AND (
                    (:expired = TRUE AND (
                        hp.currentPlanEndsAt IS NULL
                        OR hp.currentPlanEndsAt < :today
                    ))
                    OR
                    (:aboutToExpire = TRUE AND
                        hp.currentPlanEndsAt BETWEEN :today AND :plus10
                    )
                  )
            """)
    Page<Users> findAllOwnersWithExpiryOrderByLatestActivityAsc(@Param("name") String name,
                                                                @Param("expired") boolean expired,
                                                                @Param("aboutToExpire") boolean aboutToExpire,
                                                                @Param("today") LocalDateTime today,
                                                                @Param("plus10") LocalDateTime plus10,
                                                                Pageable pageable);
}
