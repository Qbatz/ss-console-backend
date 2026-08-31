package com.smartstay.console.services;

import com.smartstay.console.dao.CustomersBedHistory;
import com.smartstay.console.ennum.CustomersBedType;
import com.smartstay.console.repositories.CustomerBedHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
public class CustomerBedHistoryService {

    @Autowired
    private CustomerBedHistoryRepository customerBedHistoryRepository;

    public List<CustomersBedHistory> findByHostelIdAndCustomerIds(String hostelId, List<String> customerIds) {
        return customerBedHistoryRepository.findByHostelIdAndCustomerIds(hostelId, customerIds);
    }

    public void deleteAll(List<CustomersBedHistory> listCustomerBedHistory) {
        customerBedHistoryRepository.deleteAll(listCustomerBedHistory);
    }

    public List<CustomersBedHistory> findByHostelIdAndCustomerId(String hostelId, String customerId) {
        return customerBedHistoryRepository.findByHostelIdAndCustomerId(hostelId, customerId);
    }

    public List<CustomersBedHistory> findBedHistoriesByListOfCustomersAndDates(List<String> customerIds,
                                                                               Date startDate, Date endDate) {
        List<CustomersBedHistory> listCustomerBedHistories = customerBedHistoryRepository
                .findByListCustomerIdsAndStartAndEndDate(customerIds, startDate, endDate);
        if (listCustomerBedHistories == null) {
            listCustomerBedHistories = new ArrayList<>();
        }
        return listCustomerBedHistories;
    }

    public List<CustomersBedHistory> findBedHistoriesByCustomerIdAndDates(String customerId,
                                                                          Date startDate, Date endDate) {
        List<CustomersBedHistory> listCustomerBedHistories = customerBedHistoryRepository
                .findByCustomerIdAndStartAndEndDate(customerId, startDate, endDate);
        if (listCustomerBedHistories == null) {
            listCustomerBedHistories = new ArrayList<>();
        }
        return listCustomerBedHistories;
    }

    public CustomersBedHistory getLatestBedHistoryByCustomerId(String customerId) {
        return customerBedHistoryRepository
                .findTopByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    public List<CustomersBedHistory> getLatestBedHistoriesByCustomerIds(Set<String> customerIds) {
        return customerBedHistoryRepository.findLatestByCustomerIds(customerIds);
    }

    public void saveAll(List<CustomersBedHistory> customersBedHistoryList) {
        customerBedHistoryRepository.saveAll(customersBedHistoryList);
    }

    public List<CustomersBedHistory> getCustomerHistoriesByCustomerIdAndEndDateBefore(String customerId,
                                                                                      Date beforeDate){
        return customerBedHistoryRepository
                .findAllByCustomerIdAndEndDateBefore(customerId, beforeDate);
    }

    public List<CustomersBedHistory> getBedHistoriesByCustomerIdAndTypeNotIn(String customerId,
                                                                             String type){
        return customerBedHistoryRepository
                .findAllByCustomerIdAndTypeNot(customerId, type);
    }

    public List<CustomersBedHistory> getBedHistoriesByRoomIdsAndTypeNotIn(Set<Integer> roomIds,
                                                                          String type) {
        return customerBedHistoryRepository
                .findAllByRoomIdInAndTypeNot(roomIds, type);
    }

    public void save(CustomersBedHistory customersBedHistory) {
        customerBedHistoryRepository.save(customersBedHistory);
    }

    public List<CustomersBedHistory> getCustomersByRoomIdAndDates(Integer roomId, Date startDate,
                                                                  Date endDate) {
        return customerBedHistoryRepository.findByRoomIdStartAndEndDate(roomId, startDate, endDate);
    }

    public CustomersBedHistory getCheckInBedHistoryByCustomerId(String customerId) {
        return customerBedHistoryRepository
                .findTopByCustomerIdAndTypeOrderByIdAsc(customerId, CustomersBedType.CHECK_IN.name());
    }

    public List<CustomersBedHistory> getAllByBedIdAndBetweenDatesAndNotCustomer(int bedId, String customerId,
                                                                                Date startDate, Date endDate) {
        return customerBedHistoryRepository
                .findAllByBedIdAndBetweenDatesAndNotCustomer(bedId, customerId, startDate, endDate);
    }

    public List<CustomersBedHistory> getAllByCustomerAndBetweenDatesAndNotHistory(Long bedHistoryId, String customerId,
                                                                                  Date startDate, Date endDate) {
        return customerBedHistoryRepository
                .findAllByCustomersAndBetweenDatesAndNotHistory(bedHistoryId, customerId, startDate, endDate);
    }
}
