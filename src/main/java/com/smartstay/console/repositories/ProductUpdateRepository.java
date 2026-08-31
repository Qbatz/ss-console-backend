package com.smartstay.console.repositories;

import com.smartstay.console.dao.ProductUpdate;
import com.smartstay.console.dto.productUpdate.ProductUpdatePublishStatusCountProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductUpdateRepository extends JpaRepository<ProductUpdate, Long> {

    @Query("""
            select
                count(pu) as totalCount,
                sum(case when pu.publishStatus = 'DRAFT' then 1 else 0 end) as draftCount,
                sum(case when pu.publishStatus = 'SCHEDULED' then 1 else 0 end) as scheduledCount,
                sum(case when pu.publishStatus = 'PUBLISHED' then 1 else 0 end) as publishedCount
            from ProductUpdate pu
            where pu.isActive = true
              and pu.isDeleted = false
            """)
    ProductUpdatePublishStatusCountProjection findPublishStatusCountData();

    @Query("""
            select pu
            from ProductUpdate pu
            where pu.isActive = true
              and pu.isDeleted = false
              and (:publishStatus is null or pu.publishStatus = :publishStatus)
              and (:type is null or pu.updateType = :type)
              and (:name is null or
                  lower(replace(coalesce(pu.title, ''), ' ', ''))
                  like concat('%', lower(replace(:name, ' ', '')), '%')
              )
            order by pu.productUpdateId desc
            """)
    Page<ProductUpdate> findPagedProductUpdates(String name, String publishStatus,
                                                String type, Pageable pageable);

    ProductUpdate findByProductUpdateIdAndIsActiveTrueAndIsDeletedFalse(Long productUpdateId);
}
