package com.smartstay.console.eventListeners;

import com.smartstay.console.dao.*;
import com.smartstay.console.dto.hostel.BillingDates;
import com.smartstay.console.ennum.*;
import com.smartstay.console.events.DraftPrepaidRecurringEvents;
import com.smartstay.console.repositories.InvoiceV1Repository;
import com.smartstay.console.services.*;
import com.smartstay.console.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class DraftPrepaidRecurringEventListener {

    @Autowired
    private HostelService hostelService;
    @Autowired
    private CustomerConfigService customerConfigService;
    @Autowired
    private BookingsService bookingsService;
    @Autowired
    private CustomerWalletHistoryService customerWalletHistoryService;
    @Autowired
    private CustomersService customersService;
    @Autowired
    private ElectricityReadingsService electricityReadingsService;
    @Autowired
    private InvoiceV1Repository invoiceV1Repository;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private CustomerEbHistoryService customerEbHistoryService;
    @Autowired
    private CustomersAmenityService customersAmenityService;
    @Autowired
    private AmenitiesService amenitiesService;
    @Autowired
    private TemplatesService templatesService;
    @Autowired
    private InvoiceDraftService invoiceDraftService;

    @Async
    @EventListener
    public void draftPrepaidRecurring(DraftPrepaidRecurringEvents recurringEvents) {

        HostelV1 hostel = recurringEvents.getHostel();

        String hostelId = hostel.getHostelId();

        BillingDates billingDates = recurringEvents.getBillingDates();

        ElectricityConfig ebConfig = hostelService.getElectricityConfig(hostelId);

        boolean shouldIncludeEb;
        double flatEbAmount;
        boolean isFlatRate;
        if (ebConfig != null) {
            if (ebConfig.getTypeOfReading().equalsIgnoreCase(EBReadingType.FLAT_RATE.name())) {
                if (!ebConfig.isShouldIncludeInRent()) {
                    flatEbAmount = ebConfig.getFlatCharge();
                    isFlatRate = true;
                    shouldIncludeEb = false;
                } else {
                    shouldIncludeEb = false;
                    isFlatRate = false;
                    flatEbAmount = 0.0;
                }
            } else {
                flatEbAmount = 0.0;
                isFlatRate = false;
                shouldIncludeEb = true;
            }
        } else {
            flatEbAmount = 0.0;
            isFlatRate = false;
            shouldIncludeEb = true;
        }

        List<CustomersConfig> listCustomerConfig = customerConfigService
                .getAllActiveAndEnabledRecurringCustomers(hostelId);

        List<String> tempCusIds = listCustomerConfig.stream()
                .map(CustomersConfig::getCustomerId)
                .toList();

        List<BookingsV1> customersList = bookingsService
                .getAllCheckedInCustomersByListOfCustomerIdsAndHostelId(tempCusIds, hostelId);
        List<String> customerIds = customersList
                .stream()
                .map(BookingsV1::getCustomerId)
                .toList();

        List<CustomerWalletHistory> listCustomerWallets = customerWalletHistoryService
                .getWalletListForRecurring(customerIds);

        List<Customers> listCustomers = customersService
                .getCustomerDetails(customerIds);

        List<ElectricityReadings> listElectricityForAHostel;
        if (shouldIncludeEb) {
            listElectricityForAHostel = electricityReadingsService
                    .getAllElectricityReadingForRecurring(hostelId);
        } else {
            listElectricityForAHostel = new ArrayList<>();
        }
        List<ElectricityReadings> finalListElectricityForAHostel = listElectricityForAHostel;

        List<InvoicesV1> thisMonthInvoiceGenerated = invoiceV1Repository
                .findThisMonthInvoiceGenerated(hostelId,
                        billingDates.currentBillStartDate(), customerIds);

        List<String> generatedCustomerIds;
        if (thisMonthInvoiceGenerated != null && !thisMonthInvoiceGenerated.isEmpty()) {
            generatedCustomerIds = thisMonthInvoiceGenerated
                    .stream()
                    .map(InvoicesV1::getCustomerId)
                    .toList();
        } else {
            generatedCustomerIds = new ArrayList<>();
        }

        AtomicInteger invoiceSuffix = new AtomicInteger();

        customersList.forEach(item -> {

            if (!generatedCustomerIds.isEmpty()) {
                String invoiceGenerated = generatedCustomerIds
                        .stream()
                        .filter(i -> i.equalsIgnoreCase(item.getCustomerId()))
                        .findFirst()
                        .orElse(null);
                if (invoiceGenerated != null) {
                    return;
                }
            }

            Date joiningDate = item.getJoiningDate();
            Date billingCycleStartDate = billingDates.currentBillStartDate();

            joiningDate = Utils.getStartOfDay(joiningDate);
            billingCycleStartDate = Utils.getStartOfDay(billingCycleStartDate);

            // Skip if joined on or after billing cycle start
            if (!joiningDate.before(billingCycleStartDate)) {
                return;
            }

            Double rentAmount = 0.0;
            if (item.getRentAmount() != null) {
                rentAmount = item.getRentAmount();
            }

            Double ebAmount = 0.0;

            if (shouldIncludeEb) {
                List<Integer> ebReadingsId = finalListElectricityForAHostel
                        .stream()
                        .map(ElectricityReadings::getId)
                        .toList();

                List<CustomersEbHistory> listCustomerEb = customerEbHistoryService
                        .getAllByCustomerIdAndReadingId(item.getCustomerId(), ebReadingsId);

                ebAmount = listCustomerEb
                        .stream()
                        .mapToDouble(CustomersEbHistory::getAmount)
                        .sum();

                if (ebAmount > 0) {
                    ebAmount = Utils.roundOfDouble(ebAmount);
                }
            } else if (isFlatRate) {
                ebAmount = flatEbAmount;
            }

            List<CustomersAmenity> listCustomersAmenity = customersAmenityService
                    .getAllCustomerAmenitiesForRecurring(item.getCustomerId(),
                            billingDates.currentBillStartDate());

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

            double rentEbAmount = rentAmount + ebAmount;
            double rentEbAndAmenity = rentEbAmount + amenityAmount;
            double walletAmount = 0.0;
            double finalAmount = rentEbAndAmenity;

            Customers customers = listCustomers
                    .stream()
                    .filter(i -> i.getCustomerId().equalsIgnoreCase(item.getCustomerId()))
                    .findFirst()
                    .orElse(null);

            if (customers != null) {
                CustomerWallet customerWallet = customers.getWallet();
                if (customerWallet != null) {
                    if (customerWallet.getAmount() != null) {
                        walletAmount = customerWallet.getAmount();
                        finalAmount = finalAmount + walletAmount;
                    }
                }

                StringBuilder prefixSuffix = new StringBuilder();

                String prefix = "INV";
                BillTemplates templates = templatesService
                        .getTemplateByHostelId(customers.getHostelId());
                if (templates != null && templates.getTemplateTypes() != null) {
                    if (!templates.getTemplateTypes().isEmpty()) {
                        BillTemplateType rentTemplateType = templates.getTemplateTypes()
                                .stream()
                                .filter(i -> i.getInvoiceType()
                                        .equalsIgnoreCase(BillConfigTypes.RENTAL.name()))
                                .findFirst()
                                .get();
                        prefix = rentTemplateType.getInvoicePrefix();
                    }
                    prefixSuffix.append(prefix);
                }

                String invoiceNumber;

                if (invoiceSuffix.get() == 0) {

                    InvoicesV1 inv = invoiceV1Repository
                            .findLatestInvoiceByPrefix(prefix, hostelId);

                    if (inv != null) {
                        String[] prefArr = inv.getInvoiceNumber().split("-");

                        if (prefArr.length > 1) {
                            int suffix = Integer.parseInt(prefArr[prefArr.length - 1]);
                            invoiceSuffix.set(suffix + 1);
                        } else {
                            invoiceSuffix.set(1);
                        }
                    } else {
                        invoiceSuffix.set(1);
                    }
                } else {
                    invoiceSuffix.incrementAndGet();
                }

                invoiceNumber = String.format("%s-%03d", prefix, invoiceSuffix.get());

                Date today = new Date();

                InvoiceDrafts invoiceDrafts = new InvoiceDrafts();

                invoiceDrafts.setCustomerId(item.getCustomerId());
                invoiceDrafts.setHostelId(hostelId);
                invoiceDrafts.setInvoiceNumber(invoiceNumber);
                invoiceDrafts.setCustomerMobile(customers.getMobile());
                invoiceDrafts.setCustomerMailId(customers.getEmailId());
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
                invoiceDrafts.setInvoiceDueDate(billingDates.dueDate());
                invoiceDrafts.setInvoiceDate(today);
                invoiceDrafts.setInvoiceStartDate(billingDates.currentBillStartDate());
                invoiceDrafts.setInvoiceEndDate(billingDates.currentBillEndDate());
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
                                .equalsIgnoreCase(customers.getCustomerId()))
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
        });

        notificationService.addAdminNotificationsForDraftRecurringInvoice(hostelId);
    }
}
