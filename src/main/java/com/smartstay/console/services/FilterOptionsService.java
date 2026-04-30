package com.smartstay.console.services;

import com.smartstay.console.dao.FilterOptions;
import com.smartstay.console.repositories.FilterOptionsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FilterOptionsService {

    @Autowired
    private FilterOptionsRepository filterOptionsRepository;

    public FilterOptions getByModuleName(String moduleName) {
        return filterOptionsRepository.findByModuleName(moduleName);
    }
}
