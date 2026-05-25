package com.smartstay.console.services;

import com.smartstay.console.dao.DemoRequestComments;
import com.smartstay.console.repositories.DemoRequestCommentsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class DemoRequestCommentsService {

    @Autowired
    private DemoRequestCommentsRepository demoRequestCommentsRepository;

    public DemoRequestComments save(DemoRequestComments demoRequestComments) {
        return demoRequestCommentsRepository.save(demoRequestComments);
    }

    public List<DemoRequestComments> getDemoRequestCommentsByRequestIds(Set<Long> demoRequestIds) {
        return demoRequestCommentsRepository.findAllByRequestIdIn(demoRequestIds);
    }

    public List<DemoRequestComments> getDemoRequestCommentsByRequestId(Long requestId) {
        return demoRequestCommentsRepository.findAllByRequestId(requestId);
    }
}
