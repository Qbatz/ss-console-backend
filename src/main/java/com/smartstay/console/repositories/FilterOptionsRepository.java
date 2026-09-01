package com.smartstay.console.repositories;

import com.smartstay.console.dao.FilterOptions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FilterOptionsRepository extends JpaRepository<FilterOptions, Long> {

    FilterOptions findByModuleNameAndIsActiveTrue(String moduleName);

    FilterOptions findByFilterOptionIdAndIsActiveTrue(long filterOptionId);

    @Query("""
            select f
            from FilterOptions f
            where f.isActive = true
                and (:name is null or
                    lower(replace(coalesce(f.moduleName, ''), ' ', ''))
                        like lower(concat('%', replace(:name, ' ', ''), '%'))
                )
            """)
    Page<FilterOptions> findAllPaginated(String name, Pageable pageable);
}
