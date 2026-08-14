package com.smartstay.console.repositories;

import com.smartstay.console.dao.Users;
import com.smartstay.console.dto.users.OwnerWithAddressProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
              AND usr.isActive = true
              AND usr.isDeleted = false
            """)
    List<Users> findOwners(List<String> parentId);

    @Query("""
            SELECT usr FROM Users usr
            WHERE usr.parentId = :parentId
                AND usr.roleId = 1
                AND usr.isActive = true
                AND usr.isDeleted = false
            """)
    Users findOwner(String parentId);

    List<Users> findAllByParentIdAndIsActiveTrueAndIsDeletedFalse(String parentId);

    List<Users> findAllByParentIdAndRoleIdAndIsActiveTrueAndIsDeletedFalse(String parentId, int roleId);

    List<Users> findAllByParentIdAndRoleIdNotInAndUserIdInAndIsActiveTrueAndIsDeletedFalse(String parentId, Set<Integer> roleIds, Set<String> userIds);

    Users findByUserIdAndIsActiveTrueAndIsDeletedFalse(String userId);

    List<Users> findAllByUserIdInAndIsActiveTrueAndIsDeletedFalse(Set<String> userIds);

    @Query("""
        select u
        from Users u
        where u.isActive = true
            and u.isDeleted = false
            and (
                lower(replace(coalesce(u.firstName, ''), ' ', ''))
                    like lower(concat('%', replace(:name, ' ', ''), '%'))
                or
                lower(replace(coalesce(u.lastName, ''), ' ', ''))
                    like lower(concat('%', replace(:name, ' ', ''), '%'))
                or
                lower(concat(
                    replace(coalesce(u.firstName, ''), ' ', ''),
                    replace(coalesce(u.lastName, ''), ' ', '')
                ))
                    like lower(concat('%', replace(:name, ' ', ''), '%'))
            )
        """)
    List<Users> findByName(@Param("name") String name);

    boolean existsByEmailIdAndUserIdNotAndIsActiveTrueAndIsDeletedFalse(String email, String userId);

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
                    LOWER(REPLACE(COALESCE(u.firstName, ''), ' ', ''))
                        LIKE LOWER(CONCAT('%', REPLACE(:name, ' ', ''), '%'))
                    OR LOWER(REPLACE(COALESCE(u.lastName, ''), ' ', ''))
                        LIKE LOWER(CONCAT('%', REPLACE(:name, ' ', ''), '%'))
                    OR LOWER(CONCAT(
                        REPLACE(COALESCE(u.firstName, ''), ' ', ''),
                        REPLACE(COALESCE(u.lastName, ''), ' ', '')
                    ))
                        LIKE LOWER(CONCAT('%', REPLACE(:name, ' ', ''), '%'))
                )
                AND u.isActive = true
                AND u.isDeleted = false
            """)
    List<OwnerWithAddressProjection> findAllOwnersWithAddressProjection(@Param("name") String name);

    @Query("""
            select count(u)
            from Users u
            where u.roleId = 1
                AND u.isActive = true
                AND u.isDeleted = false
            """)
    long getCount();

    boolean existsByMobileNoAndUserIdNotAndIsActiveTrueAndIsDeletedFalse(String newMobileNo, String ownerId);

    @Query("""
            SELECT usr FROM Users usr
            WHERE usr.mobileNo = :ownerMobile
                AND usr.roleId = 1
                AND usr.isActive = true
                AND usr.isDeleted = false
            """)
    List<Users> findOwnersByMobileNo(String ownerMobile);

    @Query("""
            SELECT usr
            FROM Users usr
            WHERE usr.roleId = 1
                AND usr.isActive = true
                AND usr.isDeleted = false
                AND (
                    :name IS NULL OR :name = '' OR
                    LOWER(REPLACE(COALESCE(usr.firstName, ''), ' ', ''))
                        LIKE LOWER(CONCAT('%', REPLACE(:name, ' ', ''), '%'))
                    OR LOWER(REPLACE(COALESCE(usr.lastName, ''), ' ', ''))
                        LIKE LOWER(CONCAT('%', REPLACE(:name, ' ', ''), '%'))
                    OR LOWER(CONCAT(
                        REPLACE(COALESCE(usr.firstName, ''), ' ', ''),
                        REPLACE(COALESCE(usr.lastName, ''), ' ', '')
                    ))
                        LIKE LOWER(CONCAT('%', REPLACE(:name, ' ', ''), '%'))
                    OR REPLACE(COALESCE(usr.mobileNo, ''), ' ', '')
                        LIKE CONCAT('%', REPLACE(:name, ' ', ''), '%')
                )
            """)
    List<Users> findOwnersByMobileNoOrName(@Param("name") String name);

    @Query("""
            SELECT usr
            FROM Users usr
            WHERE usr.roleId = 1
                AND usr.parentId not in (:parentIds)
                AND usr.isActive = true
                AND usr.isDeleted = false
                AND (
                    :name IS NULL OR :name = '' OR
                    LOWER(REPLACE(COALESCE(usr.firstName, ''), ' ', ''))
                        LIKE LOWER(CONCAT('%', REPLACE(:name, ' ', ''), '%'))
                    OR LOWER(REPLACE(COALESCE(usr.lastName, ''), ' ', ''))
                        LIKE LOWER(CONCAT('%', REPLACE(:name, ' ', ''), '%'))
                    OR LOWER(CONCAT(
                        REPLACE(COALESCE(usr.firstName, ''), ' ', ''),
                        REPLACE(COALESCE(usr.lastName, ''), ' ', '')
                    ))
                        LIKE LOWER(CONCAT('%', REPLACE(:name, ' ', ''), '%'))
                    OR REPLACE(COALESCE(usr.mobileNo, ''), ' ', '')
                        LIKE CONCAT('%', REPLACE(:name, ' ', ''), '%')
                )
            """)
    List<Users> findOwnersByMobileNoOrNameAndParentIdsNot(String name, Set<String> parentIds);
}
