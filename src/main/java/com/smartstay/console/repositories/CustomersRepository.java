package com.smartstay.console.repositories;

import com.smartstay.console.dao.Customers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface CustomersRepository extends JpaRepository<Customers, String> {

    List<Customers> findAllByCustomerIdIn(Set<String> customerIds);

    Page<Customers> findAllByCustomerIdInOrderByJoiningDateDesc(Set<String> customerIds, Pageable pageable);

    @Query("""
        select c
        from Customers c
        where (
            :name is null or :name = '' or
            lower(replace(coalesce(c.firstName, ''), ' ', ''))
                like lower(concat('%', replace(:name, ' ', ''), '%')) or
            lower(replace(coalesce(c.lastName, ''), ' ', ''))
                like lower(concat('%', replace(:name, ' ', ''), '%')) or
            lower(concat(
                replace(coalesce(c.firstName, ''), ' ', ''),
                replace(coalesce(c.lastName, ''), ' ', '')
            ))
                like lower(concat('%', replace(:name, ' ', ''), '%'))
        )
        order by c.createdAt desc
        """)
    Page<Customers> findPaginatedCustomers(@Param("name") String name,
                                           Pageable pageable);

    List<Customers> findByHostelId(String hostelId);

    Customers findByCustomerIdAndHostelId(String customerId, String hostelId);

    List<Customers> findByCustomerIdIn(List<String> customerId);

    @Query("""
            select c
            from Customers c
            where
                lower(replace(coalesce(c.firstName, ''), ' ', ''))
                    like lower(concat('%', replace(:name, ' ', ''), '%'))
                or
                lower(replace(coalesce(c.lastName, ''), ' ', ''))
                    like lower(concat('%', replace(:name, ' ', ''), '%'))
                or
                lower(concat(
                    replace(coalesce(c.firstName, ''), ' ', ''),
                    replace(coalesce(c.lastName, ''), ' ', '')
                ))
                    like lower(concat('%', replace(:name, ' ', ''), '%'))
            """)
    List<Customers> findByName(@Param("name") String name);

    @Query("""
           SELECT c
           FROM Customers c
           WHERE COALESCE(c.joiningDate, c.expJoiningDate) IS NOT NULL
           AND FUNCTION('DAY', COALESCE(c.joiningDate, c.expJoiningDate)) IN :daySet
           """)
    List<Customers> findByDaySet(@Param("daySet") Set<Integer> daySet);

    boolean existsByXuidAndCustomerIdNot(String xuid, String customerId);

    @Query("""
                SELECT DISTINCT c.xuid
                FROM Customers c
                WHERE c.xuid IN :xuids
                AND c.customerId NOT IN :customerIds
            """)
    Set<String> findConflictingXuids(List<String> xuids, List<String> customerIds);

    Customers findByCustomerId(String customerId);
}
