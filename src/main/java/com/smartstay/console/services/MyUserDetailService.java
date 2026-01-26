package com.smartstay.console.services;

import com.smartstay.console.config.UserPrinciple;
import com.smartstay.console.dao.Agent;
import com.smartstay.console.repositories.AgentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailService implements UserDetailsService {

    @Autowired
    AgentRepository agentRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Agent agents = agentRepository.findByAgentId(username);

        if (agents == null) {
            return null;
        }
        return new UserPrinciple(agents);
    }

}
