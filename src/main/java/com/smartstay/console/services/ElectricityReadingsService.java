package com.smartstay.console.services;

import com.smartstay.console.dao.ElectricityReadings;
import com.smartstay.console.repositories.ElectricityReadingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ElectricityReadingsService {

    @Autowired
    private ElectricityReadingRepository electricityReadingRepository;

    public List<ElectricityReadings> findByHostelIdAndCustomerIdIn(String hostelId, List<String> customerIds) {
        return electricityReadingRepository.findByHostelId(hostelId);
    }

    public void deleteAll(List<ElectricityReadings> listElectricityReadings) {
        electricityReadingRepository.deleteAll(listElectricityReadings);
    }

    public List<ElectricityReadings> getAllElectricityReadingForRecurring(String hostelId, Date startDate, Date endDate) {
        return electricityReadingRepository
                .listAllReadingsForGenerateInvoice(hostelId, startDate, endDate);
    }

    public void markAsInvoiceGenerated(List<ElectricityReadings> listReadingForMakingInvoiceGenerated) {
        electricityReadingRepository.saveAll(listReadingForMakingInvoiceGenerated);
    }
}
