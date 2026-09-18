package com.smartstay.console.Mapper.dataArchive;

import com.smartstay.console.dao.Agent;
import com.smartstay.console.dao.DataArchive;
import com.smartstay.console.responses.dataArchive.DataArchiveResponse;
import com.smartstay.console.utils.Utils;

import java.util.Map;
import java.util.function.Function;

public class DataArchiveResMapper implements Function<DataArchive, DataArchiveResponse> {

    Map<String, Agent> agentMap;

    public DataArchiveResMapper(Map<String, Agent> agentMap) {
        this.agentMap = agentMap;
    }

    @Override
    public DataArchiveResponse apply(DataArchive dataArchive) {

        String createdBy = null;
        String restoredBy = null;
        if (agentMap != null) {
            if (dataArchive.getCreatedBy() != null) {
                Agent createdByAgent = agentMap.getOrDefault(dataArchive.getCreatedBy(), null);
                if (createdByAgent != null) {
                    createdBy = Utils.getFullName(createdByAgent.getFirstName(), createdByAgent.getLastName());
                }
            }
            if (dataArchive.getRestoredBy() != null) {
                Agent restoredByAgent = agentMap.getOrDefault(dataArchive.getRestoredBy(), null);
                if (restoredByAgent != null) {
                    restoredBy = Utils.getFullName(restoredByAgent.getFirstName(), restoredByAgent.getLastName());
                }
            }
        }

        return new DataArchiveResponse(dataArchive.getArchiveId(), dataArchive.getTableName(), dataArchive.getType().name(),
                dataArchive.getSource().name(), dataArchive.getSourceId(), dataArchive.getCriteria().name(),
                Utils.dateToString(dataArchive.getCutOffDate()), dataArchive.getRowCount(), dataArchive.getS3Bucket(),
                dataArchive.getS3Key(), dataArchive.getS3Url(), dataArchive.getFileFormat(), dataArchive.getCompression(),
                dataArchive.getChecksum(), dataArchive.getSchemaVersion(), dataArchive.getStatus().name(), dataArchive.getErrorMessage(),
                dataArchive.getCreatedBy(), createdBy, Utils.dateToString(dataArchive.getCreatedAt()),
                Utils.dateToTime(dataArchive.getCreatedAt()), dataArchive.getRestoredBy(), restoredBy,
                Utils.dateToString(dataArchive.getRestoredAt()), Utils.dateToTime(dataArchive.getRestoredAt()));
    }
}
