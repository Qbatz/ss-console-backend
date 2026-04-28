package com.smartstay.console.eventListeners;

import com.smartstay.console.dao.*;
import com.smartstay.console.dao.InvoiceItems;
import com.smartstay.console.dto.hostel.BillingDates;
import com.smartstay.console.ennum.*;
import com.smartstay.console.events.JoiningBasedPrepaidEvents;
import com.smartstay.console.repositories.InvoiceV1Repository;
import com.smartstay.console.services.*;
import com.smartstay.console.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class RecurringSetupForJoiningBasedPrepaid {

    @Autowired
    private HostelService hostelService;
    @Autowired
    private CustomersService customersService;
    @Autowired
    private CustomersAmenityService customersAmenityService;
    @Autowired
    private BookingsService bookingsService;
    @Autowired
    private TemplatesService templatesService;
    @Autowired
    private InvoiceV1Repository invoicesV1Repository;
    @Autowired
    private CustomerWalletHistoryService customerWalletHistoryService;
    @Autowired
    private CustomerRecurringTrackerService customerRecurringTrackerService;

    @Async
    @EventListener
    public void recurringSetupForJoiningBasedPrepaid(JoiningBasedPrepaidEvents jbpe) {

        HostelV1 hostelV1 = hostelService.getHostelInfo(jbpe.getHostelId());
        BookingsV1 bookingsV1 = bookingsService.getBookingInfoByCustomerId(jbpe.getCustomerId());
        Customers customers = customersService.getCustomerInformation(jbpe.getCustomerId());
        BillingDates billingDates = hostelService.getBillingRuleOnDate(jbpe.getHostelId(), new Date());
        ElectricityConfig ebConfig = hostelV1.getElectricityConfig();

        String customerId = jbpe.getCustomerId();

        if (bookingsV1 != null) {
            List<CustomerWalletHistory> listCustomerWallets = customerWalletHistoryService
                    .getAllInvoiceNotGeneratedWallets(customerId);

            double rentAmount = bookingsV1.getRentAmount();

            List<CustomersAmenity> listCustomersAmenity = customersAmenityService
                    .getAllCustomerAmenitiesForRecurring(customerId, new Date());
            Double amenityAmount = listCustomersAmenity
                    .stream()
                    .mapToDouble(CustomersAmenity::getAmenityPrice)
                    .sum();

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

            CustomerWallet customerWallet = customers.getWallet();
            if (customerWallet != null) {
                if (customerWallet.getAmount() != null) {
                    walletAmount = customerWallet.getAmount();
                    finalAmount = finalAmount + walletAmount;
                }
            }

            StringBuilder prefixSuffix = new StringBuilder();

            String prefix = "INV";
            com.smartstay.console.dao.BillTemplates templates = templatesService
                    .getTemplateByHostelId(customers.getHostelId());
            if (templates != null && templates.getTemplateTypes() != null) {
                if (!templates.getTemplateTypes().isEmpty()) {
                    BillTemplateType rentTemplateType = templates.getTemplateTypes()
                            .stream()
                            .filter(i -> i.getInvoiceType().equalsIgnoreCase(BillConfigTypes.RENTAL.name()))
                            .findFirst()
                            .get();
                    prefix = rentTemplateType.getInvoicePrefix();
                }
                prefixSuffix.append(prefix);
            }

            InvoicesV1 inv = invoicesV1Repository.findLatestInvoiceByPrefix(prefix, hostelV1.getHostelId());

            if (inv != null) {
                String[] prefArr = inv.getInvoiceNumber().split("-");
                if (prefArr.length > 1) {
                    int suffix = Integer.parseInt(prefArr[prefArr.length - 1]) + 1;
                    prefixSuffix.append("-");
                    if (suffix < 10) {
                        prefixSuffix.append("00");
                        prefixSuffix.append(suffix);
                    } else if (suffix < 100) {
                        prefixSuffix.append("0");
                        prefixSuffix.append(suffix);
                    } else {
                        prefixSuffix.append(suffix);
                    }
                }
            } else {
                //this is going to be the first invoice
                prefixSuffix.append("-");
                prefixSuffix.append("001");
            }

            Date dueDate = Utils.addDaysToDate(new Date(), billingDates.dueDays());
            int cycleStartDate = Utils.getDayOfMonth(new Date());
            Date invoiceEndDate = Utils.findLastDate(cycleStartDate, new Date());
            InvoicesV1 invoicesV1 = new InvoicesV1();
            invoicesV1.setCancelled(false);
            invoicesV1.setCustomerId(customerId);
            invoicesV1.setCustomerMailId(customers.getEmailId());
            invoicesV1.setCustomerMobile(customers.getMobile());
            invoicesV1.setHostelId(jbpe.getHostelId());
            invoicesV1.setInvoiceNumber(prefixSuffix.toString());
            invoicesV1.setInvoiceType(InvoiceType.RENT.name());
            invoicesV1.setBasePrice(finalAmount);
            invoicesV1.setTotalAmount(finalAmount);
            invoicesV1.setPaidAmount(0.0);
            invoicesV1.setCgst(0.0);
            invoicesV1.setSgst(0.0);
            invoicesV1.setGst(0.0);
            invoicesV1.setGstPercentile(0.0);
            invoicesV1.setPaymentStatus(com.smartstay.console.ennum.PaymentStatus.PENDING.name());
            invoicesV1.setOthersDescription(null);
            invoicesV1.setInvoiceMode(InvoiceMode.RECURRING.name());
            invoicesV1.setCreatedBy(hostelV1.getCreatedBy());
            invoicesV1.setInvoiceGeneratedDate(new Date());
            invoicesV1.setInvoiceDueDate(dueDate);
            invoicesV1.setInvoiceStartDate(new Date());
            invoicesV1.setInvoiceEndDate(invoiceEndDate);
            invoicesV1.setCreatedAt(new Date());

            List<InvoiceItems> invoicesItems = new ArrayList<>();
            if (rentAmount > 0) {
                InvoiceItems item1 = new InvoiceItems();
                item1.setInvoiceItem(com.smartstay.console.ennum.InvoiceItems.RENT.name());
                item1.setAmount(rentAmount);
                item1.setInvoice(invoicesV1);
                invoicesItems.add(item1);
            }

            if (ebAmount > 0) {
                InvoiceItems item1 = new InvoiceItems();
                item1.setInvoiceItem(com.smartstay.console.ennum.InvoiceItems.EB.name());
                item1.setAmount(ebAmount);
                item1.setInvoice(invoicesV1);
                invoicesItems.add(item1);
            }

            if (amenityAmount > 0) {
                InvoiceItems item1 = new InvoiceItems();
                item1.setInvoiceItem(com.smartstay.console.ennum.InvoiceItems.AMENITY.name());
                item1.setAmount(amenityAmount);
                item1.setInvoice(invoicesV1);
                invoicesItems.add(item1);
            }

            if (!listCustomerWallets.isEmpty()) {
                listCustomerWallets.forEach(it -> {
                    InvoiceItems itms = new InvoiceItems();
                    if (it.getSourceType().equalsIgnoreCase(com.smartstay.console.ennum.InvoiceItems.EB.name())) {
                        itms.setInvoiceItem(it.getSourceType());
                    } else if (it.getSourceType().equalsIgnoreCase(com.smartstay.console.ennum.InvoiceItems.AMENITY.name())) {
                        itms.setInvoiceItem(it.getSourceType());
                    } else {
                        itms.setInvoiceItem(com.smartstay.console.ennum.InvoiceItems.OTHERS.name());
                        itms.setOtherItem(it.getSourceType());
                    }
                    itms.setAmount(it.getAmount());
                    itms.setInvoice(invoicesV1);

                    invoicesItems.add(itms);
                });
            }

            invoicesV1.setInvoiceItems(invoicesItems);

            invoicesV1Repository.save(invoicesV1);

            CustomerWallet updateWallet = customers.getWallet();
            if (updateWallet != null) {
                updateWallet.setAmount(0.0);
                customers.setWallet(updateWallet);
            }

            customersService.updateCustomersFromRecurring(customers);

            if (!listCustomerWallets.isEmpty()) {
                List<CustomerWalletHistory> whu = listCustomerWallets
                        .stream()
                        .map(im -> {
                            im.setBillingStatus(WalletBillingStatus.INVOICE_GENERATED.name());
                            return im;
                        })
                        .toList();

                customerWalletHistoryService.saveAll(whu);
            }

            customerRecurringTrackerService.addToTracker(customerId, hostelV1.getHostelId(),
                    jbpe.getBillingDay(), jbpe.getBillingDates());
        }
    }
}
