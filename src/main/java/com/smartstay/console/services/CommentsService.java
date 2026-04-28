package com.smartstay.console.services;

import com.smartstay.console.dao.Comments;
import com.smartstay.console.repositories.CommentsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class CommentsService {

    @Autowired
    private CommentsRepository commentsRepository;

    public List<Comments> getByUserIds(Set<String> userIds) {
        return commentsRepository.findAllByUserIdIn(userIds);
    }

    public void deleteAll(List<Comments> comments) {
        commentsRepository.deleteAll(comments);
    }
}
