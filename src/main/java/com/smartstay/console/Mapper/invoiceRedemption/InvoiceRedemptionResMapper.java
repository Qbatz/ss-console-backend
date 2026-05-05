package com.smartstay.console.Mapper.invoiceRedemption;

import com.smartstay.console.dao.HostelV1;
import com.smartstay.console.dao.InvoiceRedemption;
import com.smartstay.console.dao.InvoicesV1;
import com.smartstay.console.dao.Users;
import com.smartstay.console.responses.invoiceRedemption.InvoiceRedemptionRes;
import com.smartstay.console.utils.Utils;

import java.util.function.Function;

public class InvoiceRedemptionResMapper implements Function<InvoiceRedemption, InvoiceRedemptionRes> {

    HostelV1 hostel;
    InvoicesV1 targetInvoice;
    InvoicesV1 sourceInvoice;
    Users createdByUser;

    public InvoiceRedemptionResMapper(HostelV1 hostel,
                                      InvoicesV1 targetInvoice,
                                      InvoicesV1 sourceInvoice,
                                      Users createdByUser) {
        this.hostel = hostel;
        this.targetInvoice = targetInvoice;
        this.sourceInvoice = sourceInvoice;
        this.createdByUser = createdByUser;
    }

    @Override
    public InvoiceRedemptionRes apply(InvoiceRedemption invoiceRedemption) {

        String hostelName = null;
        String targetInvoiceNumber = null;
        String sourceInvoiceNumber = null;
        String createdBy = null;

        if (hostel != null) {
            hostelName = hostel.getHostelName();
        }
        if (targetInvoice != null) {
            targetInvoiceNumber = targetInvoice.getInvoiceNumber();
        }
        if (sourceInvoice != null) {
            sourceInvoiceNumber = sourceInvoice.getInvoiceNumber();
        }
        if (createdByUser != null) {
            createdBy = Utils.getFullName(createdByUser.getFirstName(), createdByUser.getLastName());
        }

        return new InvoiceRedemptionRes(invoiceRedemption.getId(), invoiceRedemption.getSourceInvoiceId(), sourceInvoiceNumber,
                invoiceRedemption.getTargetInvoiceId(), targetInvoiceNumber, invoiceRedemption.getHostelId(), hostelName,
                invoiceRedemption.getRedemptionAmount(), invoiceRedemption.getReferenceNumber(), invoiceRedemption.getReason(),
                Utils.dateToString(invoiceRedemption.getRedeemedAt()), Utils.dateToTime(invoiceRedemption.getRedeemedAt()),
                Utils.dateToString(invoiceRedemption.getCreatedAt()), Utils.dateToTime(invoiceRedemption.getCreatedAt()),
                createdBy);
    }
}
