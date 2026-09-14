package com.smartstay.console.repositories;

import com.smartstay.console.dao.DataArchive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DataArchiveRepository extends JpaRepository<DataArchive, Long> {

    DataArchive findByArchiveId(Long archiveId);

    @Query("""
            select da
            from DataArchive da
            order by da.archiveId desc
            """)
    Page<DataArchive> findAllPagedArchives(Pageable pageable);
}
