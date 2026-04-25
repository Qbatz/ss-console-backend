package com.smartstay.console.services;

import com.smartstay.console.dao.DemoRequestComments;
import com.smartstay.console.repositories.DemoRequestCommentsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DemoRequestCommentsService {

    @Autowired
    private DemoRequestCommentsRepository demoRequestCommentsRepository;

    public DemoRequestComments save(DemoRequestComments demoRequestComments) {
        return demoRequestCommentsRepository.save(demoRequestComments);
    }
}
