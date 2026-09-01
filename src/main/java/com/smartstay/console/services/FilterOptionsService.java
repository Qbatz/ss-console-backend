package com.smartstay.console.services;

import com.smartstay.console.dao.FilterOptions;
import com.smartstay.console.repositories.FilterOptionsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class FilterOptionsService {

    @Autowired
    private FilterOptionsRepository filterOptionsRepository;

    public FilterOptions getByModuleName(String moduleName) {
        return filterOptionsRepository.findByModuleNameAndIsActiveTrue(moduleName);
    }

    public FilterOptions getById(long filterOptionId) {
        return filterOptionsRepository.findByFilterOptionIdAndIsActiveTrue(filterOptionId);
    }

    public Page<FilterOptions> getPaginatedFilterOptions(String name, Pageable pageable) {
        return filterOptionsRepository.findAllPaginated(name, pageable);
    }

    public FilterOptions save(FilterOptions filterOptions) {
        return filterOptionsRepository.save(filterOptions);
    }
}
