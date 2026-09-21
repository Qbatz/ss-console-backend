package com.smartstay.console.dao;

import com.smartstay.console.ennum.DataArchiveCriteriaEnum;
import com.smartstay.console.ennum.DataArchiveSourceEnum;
import com.smartstay.console.ennum.DataArchiveStatusEnum;
import com.smartstay.console.ennum.DataArchiveTypeEnum;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Data
public class DataArchive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long archiveId;
    private String tableName;
    @Enumerated(EnumType.STRING)
    private DataArchiveTypeEnum type;
    @Enumerated(EnumType.STRING)
    private DataArchiveSourceEnum source;
    private String sourceId;
    @Enumerated(EnumType.STRING)
    private DataArchiveCriteriaEnum criteria;
    private Date cutOffDate;
    private Long rowCount;
    private String s3Bucket;
    private String s3Key;
    private String s3Url;
    private String fileFormat;
    private String compression;
    private String checksum;
    private String schemaVersion;
    @Enumerated(EnumType.STRING)
    private DataArchiveStatusEnum status;
    private String errorMessage;
    private Date createdAt;
    private String createdBy;
    private Date restoredAt;
    private String restoredBy;
}
