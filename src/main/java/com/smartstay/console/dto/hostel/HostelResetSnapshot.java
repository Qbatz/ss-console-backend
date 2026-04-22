package com.smartstay.console.dto.hostel;

import com.smartstay.console.dao.*;

import java.util.List;

public record HostelResetSnapshot(HostelSnapshot hostel,
                                  List<Customers> customersList,
                                  List<InvoicesV1> invoices,
                                  List<BookingsV1> bookings,
                                  List<TransactionV1> transactions,
                                  List<CustomerWalletHistory> walletHistory,
                                  List<CreditDebitNotes> creditDebitNotes,
                                  List<ComplaintsV1> complaints,
                                  List<CustomerDocuments> documents,
                                  List<CustomerAdditionalContacts> customerAdditionalContacts,
                                  List<CustomersBedHistory> bedHistory,
                                  List<CustomersEbHistory> ebHistory,
                                  List<CustomersAmenity> amenities,
                                  List<AmenityRequest> amenityRequests,
                                  List<CustomersConfig> configs,
                                  List<CustomerCredentials> customerCredentials,
                                  List<ElectricityReadings> electricityReadings,
                                  List<HostelReadings> hostelReadings,
                                  List<Beds> beds,
                                  List<BankTransactionsV1> bankTransactions,
                                  List<BankingV1> banking) {
}
