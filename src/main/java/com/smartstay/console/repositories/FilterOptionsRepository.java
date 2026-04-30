package com.smartstay.console.repositories;

import com.smartstay.console.dao.FilterOptions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FilterOptionsRepository extends JpaRepository<FilterOptions, Long> {

    FilterOptions findByModuleName(String moduleName);
}
