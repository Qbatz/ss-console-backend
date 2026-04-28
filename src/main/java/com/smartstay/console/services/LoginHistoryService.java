package com.smartstay.console.services;

import com.smartstay.console.dao.LoginHistory;
import com.smartstay.console.repositories.LoginHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class LoginHistoryService {

    @Autowired
    private LoginHistoryRepository loginHistoryRepository;

    public List<LoginHistory> getLoginHistoriesByHostelIds(List<String> hostelIds) {
        return loginHistoryRepository.loginHistoryByParentId(hostelIds);
    }

    public void deleteAll(List<LoginHistory> loginHistories) {
        loginHistoryRepository.deleteAll(loginHistories);
    }

    public List<LoginHistory> getByUserIds(Set<String> userIds) {
        return loginHistoryRepository.findAllByUserIdIn(userIds);
    }
}
