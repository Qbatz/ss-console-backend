package com.smartstay.console.repositories;

import com.smartstay.console.dao.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface UsersRepository extends JpaRepository<Users, String> {
    Users findByEmailId(String emailId);
    @Query("""
            SELECT usr FROM Users usr WHERE usr.parentId IN (:parentId) AND usr.roleId=1
            """)
    List<Users> findOwners(List<String> parentId);

    @Query("""
            SELECT usr
            FROM Users usr
            WHERE usr.roleId = 1
              AND (
                    :name IS NULL OR :name = '' OR
                    LOWER(usr.firstName) LIKE LOWER(CONCAT('%', :name, '%'))
                    OR LOWER(usr.lastName) LIKE LOWER(CONCAT('%', :name, '%'))
                  )
            """)
    List<Users> findAllOwners(@Param("name") String name);

    @Query("""
            SELECT usr FROM Users usr WHERE usr.parentId = :parentId AND usr.roleId = 1
            """)
    Users findOwner(String parentId);

    List<Users> findAllByParentIdAndRoleId(String parentId, int roleId);

    List<Users> findAllByParentIdAndRoleIdNotInAndUserIdIn(String parentId, Set<Integer> roleIds, List<String> userIds);
}
