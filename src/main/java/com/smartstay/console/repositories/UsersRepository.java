package com.smartstay.console.repositories;

import com.smartstay.console.dao.Users;
import com.smartstay.console.dto.users.OwnerWithAddressProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface UsersRepository extends JpaRepository<Users, String> {

    @Query("""
            SELECT usr
            FROM Users usr
            WHERE usr.roleId = 1
              AND usr.parentId IN :parentId
            """)
    List<Users> findOwners(List<String> parentId);

    @Query("""
            SELECT usr FROM Users usr
            WHERE usr.parentId = :parentId
            AND usr.roleId = 1
            """)
    Users findOwner(String parentId);

    List<Users> findAllByParentIdAndRoleId(String parentId, int roleId);

    List<Users> findAllByParentIdAndRoleIdNotInAndUserIdIn(String parentId, Set<Integer> roleIds, List<String> userIds);

    Users findByUserId( String userId);

    List<Users> findAllByUserIdIn(Set<String> userIds);

    List<Users> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName);

    boolean existsByEmailIdAndUserIdNot(String email, String userId);

    @Query("""
            SELECT
                u.userId AS userId,
                u.parentId AS parentId,
                u.firstName AS firstName,
                u.lastName AS lastName,
                u.mobileNo AS mobileNo,
                u.emailId AS emailId,
                u.createdAt AS createdAt,
            
                a.addressId AS addressId,
                a.houseNo AS houseNo,
                a.street AS street,
                a.landMark AS landMark,
                a.city AS city,
                a.state AS state,
                a.pincode AS pincode
            FROM Users u
            LEFT JOIN u.address a
            WHERE u.roleId = 1
            AND (
                 :name IS NULL OR :name = '' OR
                 LOWER(u.firstName) LIKE LOWER(CONCAT('%', :name, '%'))
                 OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :name, '%'))
            )
            """)
    List<OwnerWithAddressProjection> findAllOwnersWithAddressProjection(String name);

    @Query("""
            select count(u)
            from Users u
            where u.roleId = 1
            """)
    long getCount();
}
