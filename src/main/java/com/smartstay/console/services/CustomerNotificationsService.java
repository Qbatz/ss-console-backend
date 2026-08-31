package com.smartstay.console.services;

import com.smartstay.console.config.Authentication;
import com.smartstay.console.dao.CustomerNotifications;
import com.smartstay.console.dao.Customers;
import com.smartstay.console.dao.KycDetails;
import com.smartstay.console.dao.Users;
import com.smartstay.console.ennum.NotificationType;
import com.smartstay.console.ennum.UserType;
import com.smartstay.console.repositories.CustomerNotificationsRepository;
import com.smartstay.console.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
public class CustomerNotificationsService {

    @Autowired
    private CustomerNotificationsRepository customerNotificationsRepository;
    @Autowired
    private Authentication authentication;
    @Autowired
    private FcmNotificationService fcmNotificationService;

    public void deleteAll(List<CustomerNotifications> customerNotifications) {
        customerNotificationsRepository.deleteAll(customerNotifications);
    }

    public List<CustomerNotifications> getByUserIds(Set<String> userIds) {
        return customerNotificationsRepository.findAllByUserIdIn(userIds);
    }

    public List<CustomerNotifications> findByHostelId(String hostelId) {
        return customerNotificationsRepository.findAllByHostelId(hostelId);
    }

    public void sendKycReminderNotification(Users owner, Customers customer,
                                            KycDetails kycDetails) {

        if (owner != null && customer != null && kycDetails != null){
            String titleMessage = "KYC request";
            String description = "Your hostel owner " + Utils.getFullName(owner.getFirstName(), owner.getLastName()) +
                    " wants you to complete your KYC verification. Please finish it at your earliest convenience.";

            CustomerNotifications customerNotifications = new CustomerNotifications();

            customerNotifications.setActive(true);
            customerNotifications.setNotificationType(NotificationType.KYC_REQUEST.name());
            customerNotifications.setUserId(customer.getCustomerId());
            customerNotifications.setHostelId(customer.getHostelId());
            customerNotifications.setDescription(description);
            customerNotifications.setSourceId(String.valueOf(kycDetails.getId()));
            customerNotifications.setTitle(titleMessage);
            customerNotifications.setUserType(UserType.TENANT.name());
            customerNotifications.setCreatedAt(new Date());
            customerNotifications.setCreatedBy(authentication.getName());
            customerNotifications.setActive(true);
            customerNotifications.setDeleted(false);
            customerNotifications.setRead(false);

            customerNotificationsRepository.save(customerNotifications);
            fcmNotificationService.sendKycReminderNotification(customer, kycDetails);
        }
    }
}
