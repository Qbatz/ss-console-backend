package com.smartstay.console.repositories;

import com.smartstay.console.dao.CustomerAdditionalContacts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerAdditionalContactsRepository extends JpaRepository<CustomerAdditionalContacts, Long> {

    List<CustomerAdditionalContacts> findAllByHostelIdAndCustomerId(String hostelId, String customerId);

    List<CustomerAdditionalContacts> findAllByHostelIdAndCustomerIdIn(String hostelId, List<String> customerIds);
}
