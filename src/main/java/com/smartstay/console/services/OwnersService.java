package com.smartstay.console.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.Agent;
import com.smartstay.console.dao.Users;
import com.smartstay.console.ennum.ActivityType;
import com.smartstay.console.ennum.Source;
import com.smartstay.console.payloads.owners.ResetPassword;
import com.smartstay.console.repositories.AgentRepository;
import com.smartstay.console.repositories.UsersRepository;
import com.smartstay.console.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class OwnersService {

    @Autowired
    UsersRepository usersRepository;
    @Autowired
    private Authentication authentication;
    @Autowired
    private AgentRepository agentRepository;
    @Autowired
    private AgentActivitiesService agentActivitiesService;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);

    public ResponseEntity<?> resetPassword(ResetPassword resetPassword) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Constants.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Agent agent = agentRepository.findByAgentId(authentication.getName());
        if (resetPassword == null) {
            return new ResponseEntity<>(Constants.PAYLOAD_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        if (resetPassword.password() == null) {
            return new ResponseEntity<>(Constants.PASSWORD_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        if (resetPassword.emailId() == null) {
            return new ResponseEntity<>(Constants.EMAIL_ID_REQUIRED, HttpStatus.BAD_REQUEST);
        }

        Users users = usersRepository.findByEmailId(resetPassword.emailId());
        if (users == null) {
            return new ResponseEntity<>(Constants.USER_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }
        Users oldUser = new ObjectMapper().convertValue(users, Users.class);

        String encodedPassword = encoder.encode(resetPassword.password());
        users.setPassword(encodedPassword);
        users = usersRepository.save(users);

        agentActivitiesService.createAgentActivity(agent, ActivityType.UPDATE, Source.RESET_PASSWORD,
                users.getUserId(), oldUser, users);

        return new ResponseEntity<>(Constants.UPDATED_SUCCESSFULLY, HttpStatus.OK);

    }
}
