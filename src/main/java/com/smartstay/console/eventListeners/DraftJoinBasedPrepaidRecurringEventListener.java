package com.smartstay.console.eventListeners;

import com.smartstay.console.dao.*;
import com.smartstay.console.dto.hostel.BillingDates;
import com.smartstay.console.ennum.*;
import com.smartstay.console.events.DraftJoinBasedPrepaidRecurringEvents;
import com.smartstay.console.services.*;
import com.smartstay.console.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class DraftJoinBasedPrepaidRecurringEventListener {

    @Autowired
    private BookingsService bookingsService;
    @Autowired
    private CustomerWalletHistoryService customerWalletHistoryService;
    @Autowired
    private CustomersAmenityService customersAmenityService;
    @Autowired
    private AmenitiesService amenitiesService;
    @Autowired
    private InvoiceDraftService invoiceDraftService;

    @Async
    @EventListener
    public void draftJoinBasedPrepaidRecurring(DraftJoinBasedPrepaidRecurringEvents recurringEvents) {

        HostelV1 hostel = recurringEvents.getHostel();
        String hostelId = hostel.getHostelId();
        Customers customer = recurringEvents.getCustomer();
        String customerId = customer.getCustomerId();

        String invoiceNumber = recurringEvents.getInvoiceNumber();

        BookingsV1 bookingsV1 = bookingsService.getBookingInfoByCustomerId(customerId);
        BillingDates billingDates = recurringEvents.getBillingDates();
        ElectricityConfig ebConfig = hostel.getElectricityConfig();

        if (bookingsV1 != null) {

            List<CustomerWalletHistory> listCustomerWallets = customerWalletHistoryService
                    .getAllInvoiceNotGeneratedWallets(customerId);

            double rentAmount = bookingsV1.getRentAmount() != null ? bookingsV1.getRentAmount() : 0.0;

            List<CustomersAmenity> listCustomersAmenity = customersAmenityService
                    .getAllCustomerAmenitiesForRecurring(customerId, new Date());
            Double amenityAmount = listCustomersAmenity
                    .stream()
                    .mapToDouble(CustomersAmenity::getAmenityPrice)
                    .sum();

            Set<String> amenityIds = listCustomersAmenity.stream()
                    .map(CustomersAmenity::getAmenityId)
                    .collect(Collectors.toSet());
            List<AmenitiesV1> listAmenities = amenitiesService
                    .getAmenitiesByIds(amenityIds);
            Map<String, AmenitiesV1> amenityMap = listAmenities.stream()
                    .collect(Collectors.toMap(AmenitiesV1::getAmenityId,
                            Function.identity(), (a, b) -> a));

            double ebAmount = 0.0;
            if (ebConfig != null) {
                if (ebConfig.getTypeOfReading().equalsIgnoreCase(EBReadingType.FLAT_RATE.name())) {
                    if (!ebConfig.isShouldIncludeInRent()) {
                        ebAmount = ebConfig.getFlatCharge();
                    }
                }
            }

            double rentEbAmount = rentAmount + ebAmount;
            double rentEbAndAmenity = rentEbAmount + amenityAmount;
            double walletAmount = 0.0;
            double finalAmount = rentEbAndAmenity;

            CustomerWallet customerWallet = customer.getWallet();
            if (customerWallet != null) {
                if (customerWallet.getAmount() != null) {
                    walletAmount = customerWallet.getAmount();
                    finalAmount = finalAmount + walletAmount;
                }
            }

            Date today = new Date();

            Date dueDate = Utils.addDaysToDate(today, billingDates.dueDays());
            int cycleStartDate = Utils.getDayOfMonth(today);
            Date invoiceEndDate = Utils.findLastDate(cycleStartDate, today);

            InvoiceDrafts invoiceDrafts = new InvoiceDrafts();

            invoiceDrafts.setCustomerId(customerId);
            invoiceDrafts.setHostelId(hostelId);
            invoiceDrafts.setInvoiceNumber(invoiceNumber);
            invoiceDrafts.setCustomerMobile(customer.getMobile());
            invoiceDrafts.setCustomerMailId(customer.getEmailId());
            invoiceDrafts.setInvoiceType(InvoiceType.RENT.name());
            invoiceDrafts.setBasePrice(finalAmount);
            invoiceDrafts.setTotalAmount(finalAmount);
            invoiceDrafts.setPaidAmount(0.0);
            invoiceDrafts.setBalanceAmount(0.0);
            invoiceDrafts.setSubTotal(finalAmount);
            invoiceDrafts.setGst(0.0);
            invoiceDrafts.setCgst(0.0);
            invoiceDrafts.setSgst(0.0);
            invoiceDrafts.setGstPercentile(0.0);
            invoiceDrafts.setPaymentStatus(PaymentStatus.PENDING.name());
            invoiceDrafts.setDeductionAmount(0.0);
            invoiceDrafts.setOthersDescription(null);
            invoiceDrafts.setInvoiceMode(InvoiceMode.RECURRING.name());
            invoiceDrafts.setCancelled(false);
            invoiceDrafts.setDiscounted(false);
            invoiceDrafts.setDiscountAmount(0.0);
            invoiceDrafts.setCreatedBy(hostel.getCreatedBy());
            invoiceDrafts.setUpdatedBy(null);
            invoiceDrafts.setInvoiceGeneratedDate(today);
            invoiceDrafts.setCancelledDate(null);
            invoiceDrafts.setInvoiceDueDate(dueDate);
            invoiceDrafts.setInvoiceDate(today);
            invoiceDrafts.setInvoiceStartDate(today);
            invoiceDrafts.setInvoiceEndDate(invoiceEndDate);
            invoiceDrafts.setCreatedAt(today);
            invoiceDrafts.setUpdatedAt(null);
            invoiceDrafts.setEdited(false);

            List<DraftItems> draftItems = new ArrayList<>();

            if (rentAmount > 0){
                DraftItems draftItem = new DraftItems();

                draftItem.setAmount(rentAmount);
                draftItem.setInvoiceItem(com.smartstay.console.ennum.InvoiceItems.RENT.name());
                draftItem.setInvoiceDrafts(invoiceDrafts);

                draftItems.add(draftItem);
            }

            if (ebAmount > 0){
                DraftItems draftItem = new DraftItems();

                draftItem.setAmount(ebAmount);
                draftItem.setInvoiceItem(com.smartstay.console.ennum.InvoiceItems.EB.name());
                draftItem.setInvoiceDrafts(invoiceDrafts);

                draftItems.add(draftItem);
            }

            if (listCustomersAmenity != null){
                listCustomersAmenity.forEach(i -> {
                    AmenitiesV1 amenity = amenityMap
                            .getOrDefault(i.getAmenityId(), null);
                    if (amenity != null) {
                        DraftItems draftItem = new DraftItems();

                        draftItem.setAmount(Utils.roundOfDoubleTo2Digits(i.getAmenityPrice()));
                        draftItem.setInvoiceItem(com.smartstay.console.ennum.InvoiceItems.OTHERS.name());
                        draftItem.setOtherItem(amenity.getAmenityName());
                        draftItem.setInvoiceDrafts(invoiceDrafts);

                        draftItems.add(draftItem);
                    }
                });
            }

            List<CustomerWalletHistory> wh = listCustomerWallets
                    .stream()
                    .filter(i -> i.getCustomerId()
                            .equalsIgnoreCase(customerId))
                    .toList();

            if (!wh.isEmpty()) {
                wh.forEach(i -> {
                    DraftItems draftItem = new DraftItems();

                    draftItem.setAmount(i.getAmount());
                    if (i.getSourceType().equalsIgnoreCase(
                            com.smartstay.console.ennum.InvoiceItems.EB.name())) {
                        draftItem.setInvoiceItem(i.getSourceType());
                    } else if (i.getSourceType().equalsIgnoreCase(
                            com.smartstay.console.ennum.InvoiceItems.AMENITY.name())) {
                        draftItem.setInvoiceItem(i.getSourceType());
                    } else {
                        draftItem.setInvoiceItem(
                                com.smartstay.console.ennum.InvoiceItems.OTHERS.name());
                        draftItem.setOtherItem(i.getSourceType());
                    }
                    draftItem.setInvoiceDrafts(invoiceDrafts);

                    draftItems.add(draftItem);
                });
            }

            invoiceDrafts.setListItems(draftItems);

            invoiceDraftService.save(invoiceDrafts);
        }
    }
}
