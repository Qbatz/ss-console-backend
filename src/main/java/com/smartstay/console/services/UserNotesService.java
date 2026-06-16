package com.smartstay.console.services;

import com.smartstay.console.dao.UsersNotes;
import com.smartstay.console.repositories.UserNotesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserNotesService {

    @Autowired
    private UserNotesRepository userNotesRepository;

    public List<UsersNotes> getUserNotesByUserId(String userId){
        return userNotesRepository.findAllByUserIdOrderByIdDesc(userId);
    }

    public UsersNotes save(UsersNotes userNotes){
        return userNotesRepository.save(userNotes);
    }
}
