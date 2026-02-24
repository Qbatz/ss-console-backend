package com.smartstay.console.services;

import com.smartstay.console.dao.BookingsV1;
import com.smartstay.console.repositories.BookingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookingsService {

    @Autowired
    BookingsRepository bookingsRepository;

    public List<BookingsV1> getBookingsByHostelId(String hostelId) {
        return bookingsRepository.findAllByHostelId(hostelId);
    }
}
