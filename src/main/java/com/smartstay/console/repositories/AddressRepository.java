package com.smartstay.console.repositories;

import com.smartstay.console.dao.Address;
import com.smartstay.console.dao.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Integer> {
    List<Address> findAllByUserIn(List<Users> users);
}
