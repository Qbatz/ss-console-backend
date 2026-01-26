package com.smartstay.console.config;

import com.smartstay.console.dao.Agent;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class UserPrinciple implements UserDetails {
    private final Agent agents;

    public UserPrinciple(Agent agents) {
        this.agents = agents;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority(String.valueOf(agents.getRoleId())));
    }

    @Override
    public @Nullable String getPassword() {
        return agents.getAgentEmailId();
    }

    @Override
    public String getUsername() {
        return agents.getAgentId();
    }
}
