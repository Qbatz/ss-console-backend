package com.smartstay.console.services;

import com.smartstay.console.dao.LoginHistory;
import com.smartstay.console.repositories.LoginHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LoginHistoryService {

    @Autowired
    private LoginHistoryRepository loginHistoryRepository;

    public List<LoginHistory> getLoginHistoriesByHostelIds(List<String> hostelIds) {
        return loginHistoryRepository.loginHistoryByParentId(hostelIds);
    }
}
