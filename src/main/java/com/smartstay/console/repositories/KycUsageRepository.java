package com.smartstay.console.repositories;

import com.smartstay.console.dao.KYCUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Repository
public interface KycUsageRepository extends JpaRepository<KYCUsage, Long> {

    List<KYCUsage> findAllByLatestRequestBetween(Date startDate, Date endDate);

    KYCUsage findByHostelId(String hostelId);

    List<KYCUsage> findAllByHostelIdIn(Set<String> hostelIds);

    @Query("""
            select k.hostelId
            from kycusage k
            order by k.usageId desc
            """)
    Set<String> findAllHostelIds();
}
