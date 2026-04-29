package com.smartstay.console.services;

import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.Agent;
import com.smartstay.console.dao.HostelV1;
import com.smartstay.console.dao.TableColumns;
import com.smartstay.console.dao.Users;
import com.smartstay.console.repositories.TableColumnsRepository;
import com.smartstay.console.responses.tableColumns.TableColumnsHostelResponse;
import com.smartstay.console.responses.tableColumns.TableColumnsResponse;
import com.smartstay.console.responses.tableColumns.TableColumnsUserResponse;
import com.smartstay.console.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
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
    private UsersService usersService;

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
}
