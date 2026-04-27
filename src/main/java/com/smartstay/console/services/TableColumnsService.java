package com.smartstay.console.services;

import com.smartstay.console.dao.TableColumns;
import com.smartstay.console.repositories.TableColumnsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class TableColumnsService {

    @Autowired
    private TableColumnsRepository tableTableColumnsRepository;

    public void deleteAll(List<TableColumns> tableColumns) {
        tableTableColumnsRepository.deleteAll(tableColumns);
    }

    public List<TableColumns> getByUserIds(Set<String> userIds) {
        return tableTableColumnsRepository.findAllByUserIdIn(userIds);
    }
}
