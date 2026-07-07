package com.smartstay.console.Mapper.invoice;

import com.smartstay.console.dao.Customers;
import com.smartstay.console.dao.InvoiceItems;
import com.smartstay.console.dao.InvoicesV1;
import com.smartstay.console.dao.Users;
import com.smartstay.console.ennum.InvoiceType;
import com.smartstay.console.ennum.PaymentStatus;
import com.smartstay.console.responses.invoice.InvoiceItemsResponse;
import com.smartstay.console.responses.invoice.InvoiceResponse;
import com.smartstay.console.utils.Utils;

import java.util.List;
import java.util.function.Function;

public class InvoiceResponseMapper implements Function<InvoicesV1, InvoiceResponse> {

    Customers tenant;
    Users createdByUser;
    Users updatedByUser;

    public InvoiceResponseMapper(Customers tenant,
                                 Users createdByUser,
                                 Users updatedByUser) {
        this.tenant = tenant;
        this.createdByUser = createdByUser;
        this.updatedByUser = updatedByUser;
    }

    @Override
    public InvoiceResponse apply(InvoicesV1 invoice) {

        String tenantName = null;
        if (tenant != null){
            tenantName = Utils.getFullName(tenant.getFirstName(), tenant.getLastName());
        }

        String createdBy = null;
        if (createdByUser != null) {
            createdBy = Utils.getFullName(createdByUser.getFirstName(), createdByUser.getLastName());
        }

        String updatedBy = null;
        if (updatedByUser != null) {
            updatedBy = Utils.getFullName(updatedByUser.getFirstName(), updatedByUser.getLastName());
        }

        List<InvoiceItems> invoiceItems = invoice.getInvoiceItems();

        List<InvoiceItemsResponse> invoiceItemsResponses = invoiceItems.stream()
                .map(invoiceItem -> new InvoiceItemsResponse(invoiceItem.getInvoiceItemId(),
                        invoiceItem.getInvoiceItem(), invoiceItem.getOtherItem()))
                .toList();

        boolean canShowReceipts = false;
        if (InvoiceType.ADVANCE.name().equals(invoice.getInvoiceType()) &&
                PaymentStatus.PAID.name().equals(invoice.getPaymentStatus())){
            canShowReceipts = true;
        }

        boolean canUpdateAmount = false;
        if (InvoiceType.ADVANCE.name().equals(invoice.getInvoiceType())){
            canUpdateAmount = true;
        }

        return new InvoiceResponse(invoice.getInvoiceId(), invoice.getCustomerId(), tenantName,
                invoice.getInvoiceNumber(), invoice.getCustomerMobile(), invoice.getCustomerMailId(),
                invoice.getInvoiceType(), invoice.getPaymentStatus(), invoice.getOthersDescription(),
                invoice.getInvoiceMode(), invoice.getInvoiceUrl(), invoice.isCancelled(),
                invoice.isDiscounted(), Utils.dateToString(invoice.getInvoiceGeneratedDate()),
                Utils.dateToTime(invoice.getInvoiceGeneratedDate()), Utils.dateToString(invoice.getCancelledDate()),
                Utils.dateToTime(invoice.getCancelledDate()), Utils.dateToString(invoice.getInvoiceDueDate()),
                invoice.getInvoiceType(), invoice.getPaymentStatus(), canShowReceipts, canUpdateAmount,
                invoice.getOthersDescription(), invoice.getInvoiceMode(), invoice.getInvoiceUrl(),
                invoice.isCancelled(), invoice.isDiscounted(), Utils.dateToString(invoice.getInvoiceGeneratedDate()),
                Utils.dateToTime(invoice.getInvoiceGeneratedDate()), Utils.dateToString(invoice.getInvoiceDueDate()),
                Utils.dateToTime(invoice.getInvoiceDueDate()), Utils.dateToString(invoice.getInvoiceStartDate()),
                Utils.dateToTime(invoice.getInvoiceStartDate()), Utils.dateToString(invoice.getInvoiceEndDate()),
                Utils.dateToTime(invoice.getInvoiceEndDate()), createdBy, updatedBy,
                Utils.dateToString(invoice.getCreatedAt()), Utils.dateToTime(invoice.getCreatedAt()),
                Utils.dateToString(invoice.getUpdatedAt()), Utils.dateToTime(invoice.getUpdatedAt()),
                invoiceItemsResponses);
    }
}
