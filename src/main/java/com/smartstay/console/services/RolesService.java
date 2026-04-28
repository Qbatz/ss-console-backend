package com.smartstay.console.services;

import com.smartstay.console.dao.RolesV1;
import com.smartstay.console.repositories.RolesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RolesService {

    @Autowired
    private RolesRepository rolesRepository;

    public void deleteAll(List<RolesV1> listRoles) {
        rolesRepository.deleteAll(listRoles);
    }

    public List<RolesV1> findByHostelId(String hostelId) {
        return rolesRepository.findAllByHostelId(hostelId);
    }
}
