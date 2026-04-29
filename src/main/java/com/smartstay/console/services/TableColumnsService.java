package com.smartstay.console.services;

import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.*;
import com.smartstay.console.dto.tableColumns.TableColumnsSnapshot;
import com.smartstay.console.ennum.ActivityType;
import com.smartstay.console.ennum.FilterOptionsModule;
import com.smartstay.console.ennum.Source;
import com.smartstay.console.payloads.tableColumns.EditTableColumnsPayload;
import com.smartstay.console.payloads.tableColumns.ResetTableColumnsPayload;
import com.smartstay.console.repositories.TableColumnsRepository;
import com.smartstay.console.responses.tableColumns.TableColumnsHostelResponse;
import com.smartstay.console.responses.tableColumns.TableColumnsResponse;
import com.smartstay.console.responses.tableColumns.TableColumnsUserResponse;
import com.smartstay.console.utils.SnapshotUtility;
import com.smartstay.console.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TableColumnsService {

    @Autowired
    private TableColumnsRepository tableColumnsRepository;
    @Autowired
    private Authentication authentication;
    @Autowired
    private AgentService agentService;
    @Autowired
    private AgentActivitiesService agentActivitiesService;
    @Autowired
    private HostelService hostelService;
    @Autowired
    @Lazy
    private UsersService usersService;
    @Autowired
    private FilterOptionsService filterOptionsService;

    public void deleteAll(List<TableColumns> tableColumns) {
        tableColumnsRepository.deleteAll(tableColumns);
    }

    public List<TableColumns> getByUserIds(Set<String> userIds) {
        return tableColumnsRepository.findAllByUserIdIn(userIds);
    }

    public List<TableColumns> findByHostelId(String hostelId) {
        return tableColumnsRepository.findAllByHostelId(hostelId);
    }

    public ResponseEntity<?> getTableColumns(int page, int size, String name) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        page = Math.max(page - 1, 0);
        size = Math.max(size, 1);
        name = name == null || name.isBlank() ? null : name.trim();

        Set<String> filteredHostelIds  = null;
        if (name != null) {
            List<HostelV1> hostels = hostelService.getHostelsByHostelName(name);
            filteredHostelIds  = hostels.stream()
                    .map(HostelV1::getHostelId)
                    .collect(Collectors.toSet());

            if (filteredHostelIds.isEmpty()) {

                Map<String, Object> response = new HashMap<>();
                response.put("hostelList", Collections.emptyList());
                response.put("currentPage", page + 1);
                response.put("pageSize", size);
                response.put("totalItems", 0);
                response.put("totalPages", 0);

                return new ResponseEntity<>(response, HttpStatus.OK);
            }
        }

        Pageable pageable = PageRequest.of(page, size);

        Page<String> hostelIdsPage = tableColumnsRepository
                .findDistinctHostelIds(filteredHostelIds, pageable);

        Set<String> pagedHostelIds = new HashSet<>(hostelIdsPage.getContent());

        if (pagedHostelIds.isEmpty()) {

            Map<String, Object> response = new HashMap<>();
            response.put("hostelList", Collections.emptyList());
            response.put("currentPage", page + 1);
            response.put("pageSize", size);
            response.put("totalItems", hostelIdsPage.getTotalElements());
            response.put("totalPages", hostelIdsPage.getTotalPages());

            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        Map<String, HostelV1> hostelMap = hostelService
                .getHostelsByHostelIds(pagedHostelIds)
                .stream()
                .collect(Collectors.toMap(
                        HostelV1::getHostelId,
                        hostel -> hostel
                ));

        List<TableColumns> tableColumns = tableColumnsRepository
                .findAllByHostelIdIn(pagedHostelIds);

        Set<String> userIds = tableColumns.stream()
                .map(TableColumns::getUserId)
                .collect(Collectors.toSet());

        Map<String, Users> userMap = usersService
                .getUsersByIds(userIds)
                .stream()
                .collect(Collectors.toMap(
                        Users::getUserId,
                        user -> user
                ));

        Map<String, Map<String, List<TableColumns>>> grouped = tableColumns.stream()
                        .collect(Collectors.groupingBy(
                                TableColumns::getHostelId,
                                Collectors.groupingBy(TableColumns::getUserId)
                        ));

        List<TableColumnsHostelResponse> hostelList = grouped.entrySet()
                .stream()
                .map(hostelEntry -> {

                    String hostelId = hostelEntry.getKey();

                    HostelV1 hostel = hostelMap.getOrDefault(hostelId, null);
                    String hostelName;
                    if (hostel != null) {
                        hostelName = hostel.getHostelName();
                    } else {
                        hostelName = null;
                    }

                    List<TableColumnsUserResponse> usersList =
                            hostelEntry.getValue()
                                    .entrySet()
                                    .stream()
                                    .map(userEntry -> {

                                        String userId = userEntry.getKey();

                                        Users user = userMap.getOrDefault(userId, null);
                                        String userName;
                                        if (user != null){
                                            userName = Utils.getFullName(user.getFirstName(), user.getLastName());
                                        } else {
                                            userName = null;
                                        }

                                        List<TableColumnsResponse> tableColumnsResponses =
                                                userEntry.getValue()
                                                        .stream()
                                                        .map(tableColumn -> new TableColumnsResponse(
                                                                tableColumn.getColumnId(),
                                                                tableColumn.getHostelId(),
                                                                hostelName,
                                                                tableColumn.getUserId(),
                                                                userName,
                                                                tableColumn.getModuleName(),
                                                                tableColumn.getColumns(),
                                                                Utils.dateToString(tableColumn.getCreatedAt()),
                                                                Utils.dateToTime(tableColumn.getCreatedAt()),
                                                                Utils.dateToString(tableColumn.getUpdatedAt()),
                                                                Utils.dateToTime(tableColumn.getUpdatedAt())
                                                        ))
                                                        .toList();

                                        return new TableColumnsUserResponse(
                                                userId,
                                                userName,
                                                tableColumnsResponses
                                        );
                                    })
                                    .toList();

                    return new TableColumnsHostelResponse(
                            hostelId,
                            hostelName,
                            usersList
                    );
                })
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("hostelList", hostelList);
        response.put("currentPage", page + 1);
        response.put("pageSize", size);
        response.put("totalItems", hostelIdsPage.getTotalElements());
        response.put("totalPages", hostelIdsPage.getTotalPages());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<?> updateTableColumns(EditTableColumnsPayload payload) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        List<ColumnFilters> columns = payload.columns();
        if (columns == null || columns.isEmpty()) {
            return new ResponseEntity<>(Utils.COLUMNS_CAN_NOT_BE_EMPTY, HttpStatus.BAD_REQUEST);
        }

        try {
            validateColumns(columns);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        HostelV1 hostel = hostelService.getHostelInfo(payload.hostelId());
        if (hostel == null) {
            return new ResponseEntity<>(Utils.NO_HOSTEL_FOUND, HttpStatus.BAD_REQUEST);
        }

        Users user = usersService.getUserById(payload.userId());
        if (user == null) {
            return new ResponseEntity<>(Utils.USER_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        String moduleName;
        try {
            moduleName = FilterOptionsModule.valueOf(payload.moduleName()).name();
        } catch (IllegalArgumentException | NullPointerException e) {
            return new ResponseEntity<>("Invalid module name: " + payload.moduleName(), HttpStatus.BAD_REQUEST);
        }

        TableColumns tableColumn = tableColumnsRepository
                .findByHostelIdAndUserIdAndModuleName(payload.hostelId(), payload.userId(), moduleName);
        if (tableColumn == null) {
            return new ResponseEntity<>(Utils.TABLE_COLUMN_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        TableColumnsSnapshot oldSnapshot = SnapshotUtility.toSnapshot(tableColumn);

        tableColumn.setColumns(columns);
        tableColumn.setUpdatedAt(new Date());

        tableColumn = tableColumnsRepository.save(tableColumn);

        TableColumnsSnapshot newSnapshot = SnapshotUtility.toSnapshot(tableColumn);

        agentActivitiesService.createAgentActivity(agent, ActivityType.UPDATE, Source.TABLE_COLUMNS,
                String.valueOf(tableColumn.getColumnId()), oldSnapshot, newSnapshot);

        return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);
    }

    public ResponseEntity<?> resetTableColumns(ResetTableColumnsPayload payload) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Agent agent = agentService.findUserByUserId(authentication.getName());
        if (agent == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        HostelV1 hostel = hostelService.getHostelInfo(payload.hostelId());
        if (hostel == null) {
            return new ResponseEntity<>(Utils.NO_HOSTEL_FOUND, HttpStatus.BAD_REQUEST);
        }

        Users user = usersService.getUserById(payload.userId());
        if (user == null) {
            return new ResponseEntity<>(Utils.USER_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        String moduleName;
        try {
            moduleName = FilterOptionsModule.valueOf(payload.moduleName()).name();
        } catch (IllegalArgumentException | NullPointerException e) {
            return new ResponseEntity<>("Invalid module name: " + payload.moduleName(), HttpStatus.BAD_REQUEST);
        }

        FilterOptions filterOptions = filterOptionsService.getByModuleName(moduleName);
        if (filterOptions == null) {
            return new ResponseEntity<>(Utils.FILTER_OPTION_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        List<ColumnFilters> columns = filterOptions.getFilterOptions();

        TableColumns tableColumn = tableColumnsRepository
                .findByHostelIdAndUserIdAndModuleName(payload.hostelId(), payload.userId(), moduleName);
        if (tableColumn == null) {
            return new ResponseEntity<>(Utils.TABLE_COLUMN_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }

        TableColumnsSnapshot oldSnapshot = SnapshotUtility.toSnapshot(tableColumn);

        tableColumn.setColumns(columns);
        tableColumn.setUpdatedAt(new Date());

        tableColumn = tableColumnsRepository.save(tableColumn);

        TableColumnsSnapshot newSnapshot = SnapshotUtility.toSnapshot(tableColumn);

        agentActivitiesService.createAgentActivity(agent, ActivityType.UPDATE, Source.TABLE_COLUMNS,
                String.valueOf(tableColumn.getColumnId()), oldSnapshot, newSnapshot);

        return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);
    }

    private void validateColumns(List<ColumnFilters> columns) {

        Set<Integer> orders = new HashSet<>();

        boolean hasSelectedColumn = false;

        for (ColumnFilters column : columns) {

            if (column == null) {
                throw new IllegalArgumentException(Utils.COLUMN_FILTER_CAN_NOT_BE_NULL);
            }

            if (column.getFieldName() == null || column.getFieldName().isBlank()) {
                throw new IllegalArgumentException(Utils.FIELD_NAME_IS_REQUIRED);
            }

            if (column.getOrder() < 0) {
                throw new IllegalArgumentException(Utils.ORDER_CAN_NOT_BE_NEGATIVE);
            }

            if (!orders.add(column.getOrder())) {
                throw new IllegalArgumentException(
                        "Duplicate column order: " + column.getOrder()
                );
            }

            if (column.isSelected()) {
                hasSelectedColumn = true;
            }
        }

        if (!hasSelectedColumn) {
            throw new IllegalArgumentException(Utils.AT_LEAST_ONE_COLUMN_NEEDS_TO_BE_SELECTED);
        }
    }
}
