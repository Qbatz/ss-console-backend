package com.smartstay.console.dto.customers;

import com.smartstay.console.dao.*;

import java.util.List;

public record CustomerResetSnapshot(Customers customer,
                                    List<InvoicesV1> invoices,
                                    List<BookingsV1> bookings,
                                    List<TransactionV1> transactions,
                                    List<CustomerWalletHistory> walletHistory,
                                    List<CreditDebitNotes> creditDebitNotes,
                                    List<ComplaintsV1> complaints,
                                    List<CustomerDocuments> documents,
                                    List<CustomersBedHistory> bedHistory,
                                    List<CustomersEbHistory> ebHistory,
                                    List<CustomersAmenity> amenities,
                                    List<AmenityRequest> amenityRequests,
                                    List<CustomersConfig> configs,
                                    List<BankTransactionsV1> bankTransactions,
                                    List<BankingV1> banking,
                                    List<Beds> beds) {
}
