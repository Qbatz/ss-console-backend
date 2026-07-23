package com.smartstay.console.services;

import com.smartstay.console.dao.ElectricityReadings;
import com.smartstay.console.repositories.ElectricityReadingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
public class ElectricityReadingsService {

    @Autowired
    private ElectricityReadingRepository electricityReadingRepository;

    public List<ElectricityReadings> findByHostelId(String hostelId) {
        return electricityReadingRepository.findByHostelId(hostelId);
    }

    public void deleteAll(List<ElectricityReadings> listElectricityReadings) {
        electricityReadingRepository.deleteAll(listElectricityReadings);
    }

    public List<ElectricityReadings> getAllElectricityReadingForRecurring(String hostelId) {
        return electricityReadingRepository.listAllReadingsForGenerateInvoice(hostelId);
    }

    public void markAsInvoiceGenerated(List<ElectricityReadings> listReadingForMakingInvoiceGenerated) {
        electricityReadingRepository.saveAll(listReadingForMakingInvoiceGenerated);
    }

    public List<ElectricityReadings> getPendingReadingsBetweenDates(String hostelId, int roomId,
                                                                    Date startDate, Date endDate) {
        return electricityReadingRepository.findPendingReadingsBetweenDates(hostelId, roomId, startDate, endDate);
    }

    public List<ElectricityReadings> getLatestEntriesByHostelIdAndRoomIds(String hostelId, Set<Integer> roomIds) {
        return electricityReadingRepository.findLatestEntriesByHostelIdAndRoomIds(hostelId, roomIds);
    }

    public List<ElectricityReadings> getAllByReadingIds(List<Integer> readingIds) {
        return electricityReadingRepository.findAllByIdIn(readingIds);
    }

    public void saveAll(List<ElectricityReadings> electricityReadings) {
        electricityReadingRepository.saveAll(electricityReadings);
    }
}
