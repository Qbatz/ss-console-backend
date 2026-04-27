package com.smartstay.console.services;

import com.smartstay.console.dao.ExpenseCategory;
import com.smartstay.console.repositories.ExpenseCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExpenseCategoryService {

    @Autowired
    private ExpenseCategoryRepository expenseCategoryRepository;

    public void deleteAll(List<ExpenseCategory> listExpenseCategories) {
        expenseCategoryRepository.deleteAll(listExpenseCategories);
    }

    public List<ExpenseCategory> findByHostelId(String hostelId) {
        return expenseCategoryRepository.findAllByHostelId(hostelId);
    }
}
