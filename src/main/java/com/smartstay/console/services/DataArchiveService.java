package com.smartstay.console.services;

import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartstay.console.Mapper.dataArchive.DataArchiveResMapper;
import com.smartstay.console.config.Authentication;
import com.smartstay.console.config.S3Service;
import com.smartstay.console.config.UploadFileToS3;
import com.smartstay.console.dao.Agent;
import com.smartstay.console.dao.DataArchive;
import com.smartstay.console.dao.HostelV1;
import com.smartstay.console.dao.UserActivities;
import com.smartstay.console.dto.dataArchive.DataArchiveSnapshot;
import com.smartstay.console.dto.files.S3UploadResult;
import com.smartstay.console.ennum.*;
import com.smartstay.console.repositories.DataArchiveRepository;
import com.smartstay.console.responses.dataArchive.DataArchiveResponse;
import com.smartstay.console.utils.SnapshotUtility;
import com.smartstay.console.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@Service
public class DataArchiveService {

    @Autowired
    private Authentication authentication;
    @Autowired
    private AgentService agentService;
    @Autowired
    private AgentActivitiesService agentActivitiesService;
    @Autowired
    private HostelService hostelService;
    @Autowired
    private UserActivitiesService userActivitiesService;
    @Autowired
    private UploadFileToS3 uploadFileToS3;
    @Autowired
    private DataArchiveRepository dataArchiveRepository;
    @Autowired
    private S3Service s3Service;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public ResponseEntity<?> archiveHostelActivities(String hostelId) throws IOException {

        String loggedInAgentId = authentication.getName();
        Agent loggedInAgent = agentService.findUserByUserId(loggedInAgentId);
        if (loggedInAgent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        HostelV1 hostel = hostelService.getHostelInfo(hostelId);
        if (hostel == null) {
            return new ResponseEntity<>(Utils.NO_HOSTEL_FOUND, HttpStatus.BAD_REQUEST);
        }

        int cutOffDays = 60;
        cutOffDays = cutOffDays - 1;

        Date today = new Date();
        Date cutOffDate = Utils.addDaysToDate(today, -(cutOffDays));

        int batchSize = 1000;
        int page = 0;
        long rowCount = 0;

        Pageable pageable = PageRequest.of(page, batchSize);

        Path tempFile = Files.createTempFile(
                "hostel-activities-" + hostelId + "-",
                ".jsonl.gz"
        );

        List<UserActivities> deletableUserActivities = new ArrayList<>();

        try (OutputStream fileOutputStream = Files.newOutputStream(tempFile);
             GZIPOutputStream gzipOutputStream = new GZIPOutputStream(fileOutputStream)) {

            while (true) {

                Page<UserActivities> pagedUserActivities = userActivitiesService
                        .getActivitiesByHostelIdAndBeforeDate(hostelId, cutOffDate, pageable);

                List<UserActivities> userActivities = pagedUserActivities.getContent();

                if (userActivities.isEmpty()) {
                    break;
                }

                deletableUserActivities.addAll(userActivities);

                for (UserActivities activity : userActivities) {
                    String json = objectMapper.writeValueAsString(activity);

                    gzipOutputStream.write(json.getBytes(StandardCharsets.UTF_8));
                    gzipOutputStream.write('\n');
                }

                rowCount += userActivities.size();

                if (userActivities.size() < batchSize) {
                    break;
                }

                page++;
                pageable = PageRequest.of(page, batchSize);
            }
        }

        DataArchive dataArchive = new DataArchive();

        dataArchive.setTableName("user_activities");
        dataArchive.setType(DataArchiveTypeEnum.HOSTEL_ACTIVITIES);
        dataArchive.setSource(DataArchiveSourceEnum.HOSTEL);
        dataArchive.setSourceId(hostelId);
        dataArchive.setCriteria(DataArchiveCriteriaEnum.CREATED_AT);
        dataArchive.setCutOffDate(cutOffDate);
        dataArchive.setRowCount(rowCount);

        dataArchive.setFileFormat("JSONL");
        dataArchive.setCompression("GZIP");
        dataArchive.setStatus(DataArchiveStatusEnum.IN_PROGRESS);

        dataArchive.setCreatedAt(today);
        dataArchive.setCreatedBy(loggedInAgentId);

        dataArchive = dataArchiveRepository.save(dataArchive);

        try {
            S3UploadResult s3UploadResult = uploadFileToS3.uploadArchiveToS3(
                    tempFile.toFile(), "data-archive/activities");

            dataArchive.setS3Bucket(s3UploadResult.bucket());
            dataArchive.setS3Key(s3UploadResult.key());
            dataArchive.setS3Url(s3UploadResult.url());
            dataArchive.setStatus(DataArchiveStatusEnum.BACKED_UP);

            dataArchive = dataArchiveRepository.save(dataArchive);

            userActivitiesService.deleteAll(deletableUserActivities);

            dataArchive.setStatus(DataArchiveStatusEnum.DELETED);

            dataArchive = dataArchiveRepository.save(dataArchive);
        } catch (Exception e) {

            dataArchive.setStatus(DataArchiveStatusEnum.FAILED);
            dataArchive.setErrorMessage(e.getMessage());

            dataArchive = dataArchiveRepository.save(dataArchive);

            DataArchiveSnapshot newSnapshot = SnapshotUtility.toSnapshot(dataArchive);

            agentActivitiesService.createAgentActivity(loggedInAgent, ActivityType.CREATE, Source.DATA_ARCHIVE,
                    String.valueOf(dataArchive.getArchiveId()), null, newSnapshot);

            return new ResponseEntity<>("Failed to archive hostel activities", HttpStatus.BAD_REQUEST);
        } finally {
            Files.deleteIfExists(tempFile);
        }

        DataArchiveSnapshot newSnapshot = SnapshotUtility.toSnapshot(dataArchive);

        agentActivitiesService.createAgentActivity(loggedInAgent, ActivityType.CREATE, Source.DATA_ARCHIVE,
                String.valueOf(dataArchive.getArchiveId()), null, newSnapshot);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    public ResponseEntity<?> restoreArchive(Long archiveId) {

        String loggedInAgentId = authentication.getName();
        Agent loggedInAgent = agentService.findUserByUserId(loggedInAgentId);
        if (loggedInAgent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        DataArchive dataArchive = dataArchiveRepository.findByArchiveId(archiveId);
        if (dataArchive == null){
            return new ResponseEntity<>(Utils.DATA_ARCHIVE_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        DataArchiveSnapshot oldSnapshot = SnapshotUtility.toSnapshot(dataArchive);

        if (dataArchive.getStatus() != DataArchiveStatusEnum.DELETED) {
            return new ResponseEntity<>("Archive cannot be restored in its current status", HttpStatus.BAD_REQUEST);
        }

        if (dataArchive.getS3Bucket() == null || dataArchive.getS3Key() == null) {
            return new ResponseEntity<>("Archive S3 information is missing", HttpStatus.BAD_REQUEST);
        }

        InputStream archiveInputStream = getArchiveInputStream(dataArchive);

        int batchSize = 1000;

        Date today = new Date();

        if (DataArchiveTypeEnum.HOSTEL_ACTIVITIES.equals(dataArchive.getType())) {

            if (!"user_activities".equals(dataArchive.getTableName())) {
                return new ResponseEntity<>("Restore is not supported for this archive", HttpStatus.BAD_REQUEST);
            }

            List<UserActivities> userActivities = new ArrayList<>(batchSize);

            try (InputStream inputStream = archiveInputStream;
                 GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);
                 BufferedReader reader = new BufferedReader(
                         new InputStreamReader(gzipInputStream, StandardCharsets.UTF_8)
                 )
            ) {

                String line;

                while ((line = reader.readLine()) != null) {

                    if (line.isBlank()) {
                        continue;
                    }

                    UserActivities activity = objectMapper.readValue(line, UserActivities.class);

                    userActivities.add(activity);

                    if (userActivities.size() >= batchSize) {

                        userActivitiesService.restoreUserActivities(userActivities);

                        userActivities.clear();
                    }
                }

                if (!userActivities.isEmpty()) {
                    userActivitiesService.restoreUserActivities(userActivities);
                }

                dataArchive.setStatus(DataArchiveStatusEnum.RESTORED);
                dataArchive.setRestoredAt(today);
                dataArchive.setRestoredBy(loggedInAgentId);

                dataArchive = dataArchiveRepository.save(dataArchive);

            } catch (Exception e) {

                dataArchive.setStatus(DataArchiveStatusEnum.RESTORE_PARTIAL);
                dataArchive.setErrorMessage(e.getMessage());

                dataArchive = dataArchiveRepository.save(dataArchive);

                DataArchiveSnapshot newSnapshot = SnapshotUtility.toSnapshot(dataArchive);

                agentActivitiesService.createAgentActivity(loggedInAgent, ActivityType.UPDATE, Source.DATA_ARCHIVE,
                        String.valueOf(dataArchive.getArchiveId()), oldSnapshot, newSnapshot);

                return new ResponseEntity<>("Failed to restore archive", HttpStatus.BAD_REQUEST);
            }

            DataArchiveSnapshot newSnapshot = SnapshotUtility.toSnapshot(dataArchive);

            agentActivitiesService.createAgentActivity(loggedInAgent, ActivityType.UPDATE, Source.DATA_ARCHIVE,
                    String.valueOf(dataArchive.getArchiveId()), oldSnapshot, newSnapshot);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    private InputStream getArchiveInputStream(DataArchive dataArchive) {

        try {
            S3Object s3Object = s3Service.getS3Object(dataArchive.getS3Bucket(), dataArchive.getS3Key());

            if (s3Object != null && s3Object.getObjectContent() != null) {
                return s3Object.getObjectContent();
            }

        } catch (Exception e) {
            // Fall back to S3 URL below
        }

        if (dataArchive.getS3Url() == null || dataArchive.getS3Url().isBlank()) {
            throw new RuntimeException("Unable to access archive from S3 and S3 URL is missing");
        }

        try {
            URL url = URI.create(dataArchive.getS3Url()).toURL();

            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(10_000);
            connection.setReadTimeout(60_000);

            return connection.getInputStream();

        } catch (Exception e) {
            throw new RuntimeException("Unable to download archive from S3 URL", e);
        }
    }

    public ResponseEntity<?> getDataArchives(int page, int size) {

        String loggedInAgentId = authentication.getName();
        Agent loggedInAgent = agentService.findUserByUserId(loggedInAgentId);
        if (loggedInAgent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        page = Math.max(page - 1, 0);
        size = Math.max(size, 1);

        Pageable pageable = PageRequest.of(page, size);

        Page<DataArchive> pagedDataArchives = dataArchiveRepository
                .findAllPagedArchives(pageable);

        List<DataArchive> dataArchives = pagedDataArchives.getContent();

        Set<String> agentIds = new HashSet<>();
        for (DataArchive dataArchive : dataArchives) {
            if (dataArchive.getCreatedBy() != null){
                agentIds.add(dataArchive.getCreatedBy());
            }
            if (dataArchive.getRestoredBy() != null){
                agentIds.add(dataArchive.getRestoredBy());
            }
        }

        List<Agent> agents = agentService.getAgentsByIds(agentIds);
        Map<String, Agent> agentMap = agents.stream()
                .collect(Collectors.toMap(Agent::getAgentId, a -> a));

        DataArchiveResMapper mapper = new DataArchiveResMapper(agentMap);

        List<DataArchiveResponse> responseList = dataArchives.stream()
                .map(mapper)
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("dataArchives", responseList);
        response.put("currentPage", page + 1);
        response.put("pageSize", size);
        response.put("totalItems", pagedDataArchives.getTotalElements());
        response.put("totalPages", pagedDataArchives.getTotalPages());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<?> getDataArchiveById(Long archiveId) {

        String loggedInAgentId = authentication.getName();
        Agent loggedInAgent = agentService.findUserByUserId(loggedInAgentId);
        if (loggedInAgent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        DataArchive dataArchive = dataArchiveRepository.findByArchiveId(archiveId);
        if (dataArchive == null){
            return new ResponseEntity<>(Utils.DATA_ARCHIVE_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        Set<String> agentIds = new HashSet<>();
        if (dataArchive.getCreatedBy() != null){
            agentIds.add(dataArchive.getCreatedBy());
        }
        if (dataArchive.getRestoredBy() != null){
            agentIds.add(dataArchive.getRestoredBy());
        }

        List<Agent> agents = agentService.getAgentsByIds(agentIds);
        Map<String, Agent> agentMap = agents.stream()
                .collect(Collectors.toMap(Agent::getAgentId, a -> a));

        DataArchiveResMapper mapper = new DataArchiveResMapper(agentMap);

        DataArchiveResponse response = mapper.apply(dataArchive);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
