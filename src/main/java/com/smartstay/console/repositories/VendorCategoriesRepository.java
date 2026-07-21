package com.smartstay.console.repositories;

import com.smartstay.console.dao.VendorCategories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VendorCategoriesRepository extends JpaRepository<VendorCategories, Integer> {

    List<VendorCategories> findAllByHostelId(String hostelId);
}
