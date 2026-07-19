package com.smartstay.console.utils;

import com.smartstay.console.dao.*;
import com.smartstay.console.dto.userActivities.ActivityTemplateKey;
import com.smartstay.console.ennum.ActivitySource;
import com.smartstay.console.ennum.ActivitySourceType;
import com.smartstay.console.responses.users.UserActivitiesResponse;
import com.smartstay.console.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserActivityUtil {

    @Autowired
    private UsersService usersService;
    @Autowired
    private CustomersService customersService;
    @Autowired
    private BookingsService bookingsService;
    @Autowired
    private InvoiceV1Service invoiceService;
    @Autowired
    private TransactionV1Service transactionService;

    private static final Map<ActivityTemplateKey, String> TEMPLATES = Map.<ActivityTemplateKey, String>ofEntries(

            // Customers
            Map.entry(new ActivityTemplateKey(ActivitySource.CUSTOMERS, ActivitySourceType.CHECKIN),
                    "Checked in tenant %s"),
            Map.entry(new ActivityTemplateKey(ActivitySource.CUSTOMERS, ActivitySourceType.CREATE),
                    "Added walk-in tenant %s"),
            Map.entry(new ActivityTemplateKey(ActivitySource.CUSTOMERS, ActivitySourceType.UPDATE),
                    "Updated tenant %s"),
            Map.entry(new ActivityTemplateKey(ActivitySource.CUSTOMERS, ActivitySourceType.ASSIGN),
                    "Assigned a bed for %s"),
            Map.entry(new ActivityTemplateKey(ActivitySource.CUSTOMERS, ActivitySourceType.BOOKING),
                    "Created a booking for %s"),
            Map.entry(new ActivityTemplateKey(ActivitySource.CUSTOMERS, ActivitySourceType.NOTICE),
                    "Moved tenant %s to notice"),
            Map.entry(new ActivityTemplateKey(ActivitySource.CUSTOMERS, ActivitySourceType.SETTLEMENT),
                    "Generated settlement for %s"),
            Map.entry(new ActivityTemplateKey(ActivitySource.CUSTOMERS, ActivitySourceType.CHANGED_BED),
                    "Changed bed for %s"),
            Map.entry(new ActivityTemplateKey(ActivitySource.CUSTOMERS, ActivitySourceType.CANCEL),
                    "Cancelled checkout for %s"),
            Map.entry(new ActivityTemplateKey(ActivitySource.CUSTOMERS, ActivitySourceType.DELETE),
                    "Deleted tenant %s"),
            Map.entry(new ActivityTemplateKey(ActivitySource.CUSTOMERS, ActivitySourceType.FILES_UPLOAD),
                    "Uploaded files for %s"),
            Map.entry(new ActivityTemplateKey(ActivitySource.CUSTOMERS, ActivitySourceType.FILE_DELETE),
                    "Deleted file for %s"),

            // Bookings
            Map.entry(new ActivityTemplateKey(ActivitySource.BOOKING, ActivitySourceType.CANCEL),
                    "Cancelled booking for %s"),
            Map.entry(new ActivityTemplateKey(ActivitySource.BOOKING, ActivitySourceType.CHECKOUT),
                    "Checked out tenant %s"),
            Map.entry(new ActivityTemplateKey(ActivitySource.BOOKING, ActivitySourceType.UPDATE_AMOUNT),
                    "Updated rent amount for %s"),
            Map.entry(new ActivityTemplateKey(ActivitySource.BOOKING, ActivitySourceType.JOINING_DATE),
                    "Updated joining date for %s"),
            Map.entry(new ActivityTemplateKey(ActivitySource.BOOKING, ActivitySourceType.ADVANCE_AMOUNT),
                    "Updated advance amount for %s"),

            // Invoice
            Map.entry(new ActivityTemplateKey(ActivitySource.INVOICE, ActivitySourceType.MANUAL_BILL),
                    "Created manual invoice %s"),
            Map.entry(new ActivityTemplateKey(ActivitySource.INVOICE, ActivitySourceType.UPDATE),
                    "Updated invoice %s"),
            Map.entry(new ActivityTemplateKey(ActivitySource.INVOICE, ActivitySourceType.MARK_UNPAID),
                    "Marked invoice %s as unpaid"),
            Map.entry(new ActivityTemplateKey(ActivitySource.INVOICE, ActivitySourceType.DISCOUNT),
                    "Applied discount to invoice %s"),
            Map.entry(new ActivityTemplateKey(ActivitySource.INVOICE, ActivitySourceType.EDIT_DISCOUNT),
                    "Modified discount for invoice %s"),
            Map.entry(new ActivityTemplateKey(ActivitySource.INVOICE, ActivitySourceType.DELETE_DISCOUNT),
                    "Deleted discount from invoice %s"),
            Map.entry(new ActivityTemplateKey(ActivitySource.INVOICE, ActivitySourceType.REDEEMED),
                    "Redeemed booking/advance amount for invoice %s"),

            // Transactions
            Map.entry(new ActivityTemplateKey(ActivitySource.TRANSACTIONS, ActivitySourceType.CREATE),
                    "Received payment for %s"),
            Map.entry(new ActivityTemplateKey(ActivitySource.TRANSACTIONS, ActivitySourceType.REFUND),
                    "Refunded payment for %s"),
            Map.entry(new ActivityTemplateKey(ActivitySource.TRANSACTIONS, ActivitySourceType.DELETE),
                    "Deleted receipt for %s"),
            Map.entry(new ActivityTemplateKey(ActivitySource.TRANSACTIONS, ActivitySourceType.DOWNLOAD),
                    "Downloaded receipt PDF for %s"),
            Map.entry(new ActivityTemplateKey(ActivitySource.TRANSACTIONS, ActivitySourceType.WHATSAPP),
                    "Shared receipt through WhatsApp for %s"),

            // Profile
            Map.entry(new ActivityTemplateKey(ActivitySource.PROFILE, ActivitySourceType.LOGGED_IN),
                    "Logged in to SmartStay"),
            Map.entry(new ActivityTemplateKey(ActivitySource.PROFILE, ActivitySourceType.CREATE),
                    "Created SmartStay account"),
            Map.entry(new ActivityTemplateKey(ActivitySource.PROFILE, ActivitySourceType.SETUP),
                    "Setup login PIN"),
            Map.entry(new ActivityTemplateKey(ActivitySource.PROFILE, ActivitySourceType.ADD_ADMIN),
                    "Added admin user"),
            Map.entry(new ActivityTemplateKey(ActivitySource.PROFILE, ActivitySourceType.ADD_USER),
                    "Added user"),
            Map.entry(new ActivityTemplateKey(ActivitySource.PROFILE, ActivitySourceType.UPDATE),
                    "Updated profile information"),
            Map.entry(new ActivityTemplateKey(ActivitySource.PROFILE, ActivitySourceType.CHANGE_SELF_PASSWORD),
                    "Changed password"),
            Map.entry(new ActivityTemplateKey(ActivitySource.PROFILE, ActivitySourceType.CHANGE_ADMIN_PASSWORD),
                    "Changed admin password"),
            Map.entry(new ActivityTemplateKey(ActivitySource.PROFILE, ActivitySourceType.DELETE_ADMIN_USER),
                    "Deleted admin user"),
            Map.entry(new ActivityTemplateKey(ActivitySource.PROFILE, ActivitySourceType.LOGOUT),
                    "Logged out from application"
            ),

            //Amenity
            Map.entry(new ActivityTemplateKey(ActivitySource.AMENITY, ActivitySourceType.CREATE),
                    "Created amenity"),
            Map.entry(new ActivityTemplateKey(ActivitySource.AMENITY, ActivitySourceType.ASSIGN),
                    "Assigned amenity"),
            Map.entry(new ActivityTemplateKey(ActivitySource.AMENITY, ActivitySourceType.UPDATE),
                    "Updated amenity"),
            Map.entry(new ActivityTemplateKey(ActivitySource.AMENITY, ActivitySourceType.UNASSIGN),
                    "Unassigned amenity"),
            Map.entry(new ActivityTemplateKey(ActivitySource.AMENITY, ActivitySourceType.DELETE),
                    "Deleted amenity"),

            //Settlement
            Map.entry(new ActivityTemplateKey(ActivitySource.SETTLEMENT, ActivitySourceType.CREATE),
                    "Generated final settlement"),
            Map.entry(new ActivityTemplateKey(ActivitySource.SETTLEMENT, ActivitySourceType.UPDATE),
                    "Updated final settlement"),

            //Assets
            Map.entry(new ActivityTemplateKey(ActivitySource.ASSETS, ActivitySourceType.CREATE),
                    "Added new asset"),
            Map.entry(new ActivityTemplateKey(ActivitySource.ASSETS, ActivitySourceType.UPDATE),
                    "Updated asset"),

            //Expense
            Map.entry(new ActivityTemplateKey(ActivitySource.EXPENSE, ActivitySourceType.CREATE),
                    "Created expense"),
            Map.entry(new ActivityTemplateKey(ActivitySource.EXPENSE, ActivitySourceType.UPDATE),
                    "Updated expense"),
            Map.entry(new ActivityTemplateKey(ActivitySource.EXPENSE, ActivitySourceType.DELETE),
                    "Deleted expense"),

            //Expense category
            Map.entry(new ActivityTemplateKey(ActivitySource.EXPENSE_CATEGORY, ActivitySourceType.CREATE),
                    "Created expense category"),
            Map.entry(new ActivityTemplateKey(ActivitySource.EXPENSE_CATEGORY, ActivitySourceType.UPDATE),
                    "Updated expense category"),

            //Expense sub category
            Map.entry(new ActivityTemplateKey(ActivitySource.EXPENSE_SUB_CATEGORY, ActivitySourceType.CREATE),
                    "Created expense subcategory"),
            Map.entry(new ActivityTemplateKey(ActivitySource.EXPENSE_SUB_CATEGORY, ActivitySourceType.UPDATE),
                    "Updated expense subcategory"),

            //Banking
            Map.entry(new ActivityTemplateKey(ActivitySource.BANKING, ActivitySourceType.CREATE),
                    "Added bank account"),
            Map.entry(new ActivityTemplateKey(ActivitySource.BANKING, ActivitySourceType.UPDATE),
                    "Updated bank account"),
            Map.entry(new ActivityTemplateKey(ActivitySource.BANKING, ActivitySourceType.ADD_MONEY),
                    "Added money to bank account"),
            Map.entry(new ActivityTemplateKey(ActivitySource.BANKING, ActivitySourceType.TRANSFER),
                    "Transferred money"),

            //Beds
            Map.entry(new ActivityTemplateKey(ActivitySource.BEDS, ActivitySourceType.CREATE),
                    "Created bed"),
            Map.entry(new ActivityTemplateKey(ActivitySource.BEDS, ActivitySourceType.UPDATE),
                    "Updated bed"),
            Map.entry(new ActivityTemplateKey(ActivitySource.BEDS, ActivitySourceType.UPDATE_AMOUNT),
                    "Updated bed amount"),
            Map.entry(new ActivityTemplateKey(ActivitySource.BEDS, ActivitySourceType.DELETE),
                    "Deleted bed"),

            //Complaints
            Map.entry(new ActivityTemplateKey(ActivitySource.COMPLAINTS, ActivitySourceType.CREATE),
                    "Raised complaint"),

            //Comments
            Map.entry(new ActivityTemplateKey(ActivitySource.COMMENTS, ActivitySourceType.CREATE),
                    "Added comment"),
            Map.entry(new ActivityTemplateKey(ActivitySource.COMMENTS, ActivitySourceType.UPDATE),
                    "Updated complaint"),
            Map.entry(new ActivityTemplateKey(ActivitySource.COMMENTS, ActivitySourceType.ASSIGN),
                    "Assigned complaint"),
            Map.entry(new ActivityTemplateKey(ActivitySource.COMMENTS, ActivitySourceType.DELETE),
                    "Deleted complaint"),

            //Complaint type
            Map.entry(new ActivityTemplateKey(ActivitySource.COMPLAINT_TYPE, ActivitySourceType.CREATE),
                    "Created complaint type"),
            Map.entry(new ActivityTemplateKey(ActivitySource.COMPLAINT_TYPE, ActivitySourceType.UPDATE),
                    "Updated complaint type"),
            Map.entry(new ActivityTemplateKey(ActivitySource.COMPLAINT_TYPE, ActivitySourceType.DELETE),
                    "Deleted complaint type"),

            //Electricity
            Map.entry(new ActivityTemplateKey(ActivitySource.ELECTRICITY, ActivitySourceType.CREATE),
                    "Added electricity reading"),
            Map.entry(new ActivityTemplateKey(ActivitySource.ELECTRICITY, ActivitySourceType.UPDATE),
                    "Updated electricity date"),
            Map.entry(new ActivityTemplateKey(ActivitySource.ELECTRICITY, ActivitySourceType.UPDATE_READING),
                    "Updated electricity reading"),
            Map.entry(new ActivityTemplateKey(ActivitySource.ELECTRICITY, ActivitySourceType.DELETE),
                    "Deleted electricity entry"),

            //Floors
            Map.entry(new ActivityTemplateKey(ActivitySource.FLOORS, ActivitySourceType.CREATE),
                    "Created floor"),
            Map.entry(new ActivityTemplateKey(ActivitySource.FLOORS, ActivitySourceType.UPDATE),
                    "Updated floor"),
            Map.entry(new ActivityTemplateKey(ActivitySource.FLOORS, ActivitySourceType.DELETE),
                    "Deleted floor"),

            //Hostel
            Map.entry(new ActivityTemplateKey(ActivitySource.HOSTEL, ActivitySourceType.CREATE),
                    "Created hostel"),
            Map.entry(new ActivityTemplateKey(ActivitySource.HOSTEL, ActivitySourceType.REMOVE_USER),
                    "Removed user"),
            Map.entry(new ActivityTemplateKey(ActivitySource.HOSTEL, ActivitySourceType.DELETE),
                    "Deleted hostel"),
            Map.entry(new ActivityTemplateKey(ActivitySource.HOSTEL, ActivitySourceType.UPDATE_EB_AMOUNT),
                    "Updated EB amount"),
            Map.entry(new ActivityTemplateKey(ActivitySource.HOSTEL, ActivitySourceType.UPDATE_EB_CONFIG),
                    "Updated EB configuration"),
            Map.entry(new ActivityTemplateKey(ActivitySource.HOSTEL, ActivitySourceType.UPDATE_BILLING_CONFIG),
                    "Updated billing configuration"),
            Map.entry(new ActivityTemplateKey(ActivitySource.HOSTEL, ActivitySourceType.UPDATE),
                    "Updated hostel information"),

            //Role
            Map.entry(new ActivityTemplateKey(ActivitySource.ROLE, ActivitySourceType.CREATE),
                    "Created role"),
            Map.entry(new ActivityTemplateKey(ActivitySource.ROLE, ActivitySourceType.UPDATE),
                    "Updated role"),
            Map.entry(new ActivityTemplateKey(ActivitySource.ROLE, ActivitySourceType.DELETE),
                    "Deleted role"),

            //Rooms
            Map.entry(new ActivityTemplateKey(ActivitySource.ROOMS, ActivitySourceType.CREATE),
                    "Created room"),
            Map.entry(new ActivityTemplateKey(ActivitySource.ROOMS, ActivitySourceType.UPDATE),
                    "Updated room"),
            Map.entry(new ActivityTemplateKey(ActivitySource.ROOMS, ActivitySourceType.DELETE),
                    "Deleted room"),

            //Subscription
            Map.entry(new ActivityTemplateKey(ActivitySource.SUBSCRIPTION, ActivitySourceType.CREATE),
                    "Extended subscription"),

            //Templates
            Map.entry(new ActivityTemplateKey(ActivitySource.TEMPLATES, ActivitySourceType.UPDATE),
                    "Updated hostel template"),
            Map.entry(new ActivityTemplateKey(ActivitySource.TEMPLATES, ActivitySourceType.DELETE),
                    "Deleted hostel template"),
            Map.entry(new ActivityTemplateKey(ActivitySource.TEMPLATES, ActivitySourceType.DELETE_OTHER_FIELDS),
                    "Deleted template field"),

            //Payments
            Map.entry(new ActivityTemplateKey(ActivitySource.PAYMENTS, ActivitySourceType.CREATE_SESSION),
                    "Created payment session for subscription")
    );

    public List<UserActivitiesResponse> buildResponses(List<UserActivities> activities) {

        if (activities == null || activities.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, String> displayValues =
                buildDisplayValues(activities);

        Map<String, String> userNames =
                buildUserNames(activities);

        return activities.stream()
                .map(activity -> buildResponse(
                        activity,
                        displayValues,
                        userNames
                ))
                .toList();
    }

    private UserActivitiesResponse buildResponse(UserActivities activity,
                                                 Map<String, String> displayValues,
                                                 Map<String, String> userNames) {

        return new UserActivitiesResponse(
                activity.getActivityId(),
                buildDescription(activity, displayValues),
                activity.getUserId(),
                userNames.get(activity.getUserId()),
                Utils.dateToString(activity.getLoggedAt()),
                Utils.dateToTime(activity.getLoggedAt()),
                activity.getSourceId(),
                activity.getSource(),
                activity.getActivityType(),
                activity.getPlatform()
        );
    }

    private String buildDescription(UserActivities activity,
                                    Map<String, String> displayValues) {

        ActivitySource activitySource;
        ActivitySourceType activitySourceType;
        try {
            activitySource = ActivitySource.valueOf(activity.getSource());
            activitySourceType = ActivitySourceType.valueOf(activity.getActivityType());
        } catch (IllegalArgumentException e) {
            return activity.getDescription();
        }

        ActivityTemplateKey key = new ActivityTemplateKey(
                activitySource,
                activitySourceType
        );

        String template = TEMPLATES.get(key);

        if (template == null) {
            return activity.getDescription();
        }

        if (!template.contains("%s")) {
            return template;
        }

        String value = displayValues.getOrDefault(
                activity.getSourceId(),
                "Unknown"
        );

        return String.format(template, value);
    }

    private Map<String, String> buildUserNames(List<UserActivities> activities) {

        Set<String> userIds = activities.stream()
                .map(UserActivities::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return usersService.getUsersByIds(userIds)
                .stream()
                .collect(Collectors.toMap(
                        Users::getUserId,
                        users -> Utils
                                .getFullName(users.getFirstName(), users.getLastName())
                ));
    }

    private Map<String, String> buildDisplayValues(List<UserActivities> activities) {

        Map<ActivitySource, Set<String>> idsBySource =
                collectIdsBySource(activities);

        Map<String, String> displayValues = new HashMap<>();

        loadCustomers(idsBySource, displayValues);
        loadBookings(idsBySource, displayValues);
        loadInvoices(idsBySource, displayValues);
        loadTransactions(idsBySource, displayValues);

        return displayValues;
    }

    private Map<ActivitySource, Set<String>> collectIdsBySource(List<UserActivities> activities) {

        Map<ActivitySource, Set<String>> idsBySource = new EnumMap<>(ActivitySource.class);

        for (UserActivities activity : activities) {

            if (activity.getSourceId() == null ||
                    activity.getSourceId().isBlank()) {
                continue;
            }

            try {

                ActivitySource source =
                        ActivitySource.valueOf(
                                activity.getSource());

                idsBySource
                        .computeIfAbsent(
                                source,
                                k -> new HashSet<>())
                        .add(activity.getSourceId());

            } catch (IllegalArgumentException ignored) {
            }
        }

        return idsBySource;
    }

    private void loadCustomers(Map<ActivitySource, Set<String>> idsBySource,
                               Map<String, String> displayValues) {

        Set<String> customerIds =
                idsBySource.getOrDefault(
                        ActivitySource.CUSTOMERS,
                        Collections.emptySet());

        if (customerIds.isEmpty()) {
            return;
        }

        customersService
                .getCustomersByIds(customerIds)
                .forEach(customer ->
                        displayValues.put(
                                customer.getCustomerId(),
                                Utils.getFullName(customer.getFirstName(), customer.getLastName())
                        ));
    }

    private void loadBookings(Map<ActivitySource, Set<String>> idsBySource,
                              Map<String, String> displayValues) {

        Set<String> bookingIds =
                idsBySource.getOrDefault(
                        ActivitySource.BOOKING,
                        Collections.emptySet());

        if (bookingIds.isEmpty()) {
            return;
        }

        List<BookingsV1> bookings =
                bookingsService.getBookingsByBookingIds(bookingIds);

        Set<String> customerIds = bookings.stream()
                .map(BookingsV1::getCustomerId)
                .collect(Collectors.toSet());

        Map<String, String> customerNames =
                customersService.getCustomersByIds(customerIds)
                        .stream()
                        .collect(Collectors.toMap(
                                Customers::getCustomerId,
                                customer -> Utils.getFullName(
                                        customer.getFirstName(),
                                        customer.getLastName()
                                )
                        ));

        bookings.forEach(booking ->
                displayValues.put(
                        booking.getBookingId(),
                        customerNames.getOrDefault(
                                booking.getCustomerId(),
                                ""
                        )
                ));
    }

    private void loadInvoices(Map<ActivitySource, Set<String>> idsBySource,
                              Map<String, String> displayValues) {

        Set<String> invoiceIds =
                idsBySource.getOrDefault(
                        ActivitySource.INVOICE,
                        Collections.emptySet());

        if (invoiceIds.isEmpty()) {
            return;
        }

        invoiceService
                .getInvoicesByIds(invoiceIds)
                .forEach(invoice ->
                        displayValues.put(
                                invoice.getInvoiceId(),
                                invoice.getInvoiceNumber()
                        ));
    }

    private void loadTransactions(Map<ActivitySource, Set<String>> idsBySource,
                                  Map<String, String> displayValues) {

        Set<String> transactionIds =
                idsBySource.getOrDefault(
                        ActivitySource.TRANSACTIONS,
                        Collections.emptySet());

        if (transactionIds.isEmpty()) {
            return;
        }

        List<TransactionV1> transactions =
                transactionService.getByTransactionIds(transactionIds);

        Set<String> customerIds = transactions.stream()
                .map(TransactionV1::getCustomerId)
                .collect(Collectors.toSet());

        Map<String, String> customerNames =
                customersService.getCustomersByIds(customerIds)
                        .stream()
                        .collect(Collectors.toMap(
                                Customers::getCustomerId,
                                customer -> Utils.getFullName(
                                        customer.getFirstName(),
                                        customer.getLastName()
                                )
                        ));

        transactions.forEach(transaction ->
                displayValues.put(
                        transaction.getTransactionId(),
                        customerNames.getOrDefault(
                                transaction.getCustomerId(),
                                ""
                        )
                ));
    }
}
