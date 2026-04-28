package com.smartstay.console.dto.hostel;

import com.smartstay.console.dto.customers.CustomersCredentialsSnapshot;

import java.util.List;

public record HostelResetSnapshot(HostelSnapshot hostel,
                                  List<CustomersCredentialsSnapshot> customerCredentials) {
}
