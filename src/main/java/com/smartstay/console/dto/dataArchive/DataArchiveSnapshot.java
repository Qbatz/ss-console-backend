package com.smartstay.console.dto.dataArchive;

import com.smartstay.console.ennum.DataArchiveCriteriaEnum;
import com.smartstay.console.ennum.DataArchiveSourceEnum;
import com.smartstay.console.ennum.DataArchiveStatusEnum;
import com.smartstay.console.ennum.DataArchiveTypeEnum;

import java.util.Date;

public record DataArchiveSnapshot(Long archiveId,
                                  String tableName,
                                  DataArchiveTypeEnum type,
                                  DataArchiveSourceEnum source,
                                  String sourceId,
                                  DataArchiveCriteriaEnum criteria,
                                  Date cutOffDate,
                                  Long rowCount,
                                  String s3Bucket,
                                  String s3Key,
                                  String s3Url,
                                  String fileFormat,
                                  String compression,
                                  String checksum,
                                  String schemaVersion,
                                  DataArchiveStatusEnum status,
                                  String errorMessage,
                                  Date createdAt,
                                  String createdBy,
                                  Date restoredAt,
                                  String restoredBy) {
}
