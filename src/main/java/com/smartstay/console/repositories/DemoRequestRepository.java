package com.smartstay.console.repositories;

import com.smartstay.console.dao.DemoRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DemoRequestRepository extends JpaRepository<DemoRequest, Long> {

    @Query("""
            select dr from DemoRequest dr
            where (
                :name is null or :name = '' or
                lower(dr.name) like lower(concat('%', :name, '%'))
            )
            order by dr.requestId desc
            """)
    Page<DemoRequest> findAllPaginated(@Param("name") String name, Pageable pageable);

    DemoRequest findByRequestId(Long demoRequestId);

    @Query("""
            select count(dr)
            from DemoRequest dr
            """)
    long getCount();
}
