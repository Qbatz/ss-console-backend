package com.smartstay.console.services;

import com.smartstay.console.dao.UserHostel;
import com.smartstay.console.repositories.UserHostelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class UserHostelService {

    @Autowired
    private UserHostelRepository userHostelRepository;

    public List<UserHostel> getUsersByHostelId(String hostelId) {
        return userHostelRepository.findAllByHostelId(hostelId);
    }

    public List<UserHostel> getUsersByParentId(String parentId) {
        return userHostelRepository.findAllByParentId(parentId);
    }

    public void deleteAll(List<UserHostel> userHostels) {
        userHostelRepository.deleteAll(userHostels);
    }

    public boolean existsByHostelIdAndUserId(String hostelId, String userId) {
        return userHostelRepository.existsByHostelIdAndUserId(hostelId, userId);
    }

    public List<UserHostel> getUserHostelsByParentIds(Set<String> parentIds) {
        return userHostelRepository.findAllByParentIdIn(parentIds);
    }
}
