package com.smartstay.console.services;

import com.smartstay.console.dao.Address;
import com.smartstay.console.dao.Users;
import com.smartstay.console.repositories.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressService {

    @Autowired
    private AddressRepository addressRepository;

    public List<Address> getAddressByUsers(List<Users> users) {
        return addressRepository.findAllByUserIn(users);
    }
}
