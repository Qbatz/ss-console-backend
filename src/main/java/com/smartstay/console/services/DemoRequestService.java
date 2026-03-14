package com.smartstay.console.services;

import com.smartstay.console.Mapper.demoRequests.DemoRequestMapper;
import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.Agent;
import com.smartstay.console.dao.DemoRequest;
import com.smartstay.console.repositories.DemoRequestRepository;
import com.smartstay.console.responses.demoRequest.DemoRequestResponse;
import com.smartstay.console.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DemoRequestService {

    @Autowired
    private Authentication authentication;
    @Autowired
    private DemoRequestRepository demoRequestRepository;
    @Autowired
    private AgentService agentService;

    public ResponseEntity<?> getAllDemoRequests(int page, int size, String name){

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
        Pageable pageable = PageRequest.of(page, size);

        Page<DemoRequest> paginatedDemoRequest = demoRequestRepository
                .findAllPaginated(name, pageable);

        List<DemoRequest> demoRequests = paginatedDemoRequest.getContent();

        List<DemoRequestResponse> demoRequestList = demoRequests.stream()
                .map(demoRequest -> new DemoRequestMapper().apply(demoRequest))
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("demoRequestList", demoRequestList);
        response.put("currentPage", page + 1);
        response.put("pageSize", size);
        response.put("totalItems", paginatedDemoRequest.getTotalElements());
        response.put("totalPages", paginatedDemoRequest.getTotalPages());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
