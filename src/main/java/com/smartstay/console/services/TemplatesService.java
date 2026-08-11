package com.smartstay.console.services;

import com.smartstay.console.dao.BillTemplateType;
import com.smartstay.console.dao.BillTemplates;
import com.smartstay.console.dto.billTemplates.BillTemplatesDto;
import com.smartstay.console.ennum.BillConfigTypes;
import com.smartstay.console.ennum.InvoiceType;
import com.smartstay.console.repositories.BillTemplatesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TemplatesService {

    @Autowired
    BillTemplatesRepository billTemplatesRepository;

    public BillTemplates getTemplateByHostelId(String hostelId) {
        return billTemplatesRepository.getByHostelId(hostelId);
    }

    public void deleteAll(List<BillTemplates> listBillTemplates) {
        billTemplatesRepository.deleteAll(listBillTemplates);
    }

    public List<BillTemplates> findByHostelId(String hostelId) {
        return billTemplatesRepository.findAllByHostelId(hostelId);
    }

    public BillTemplatesDto getBillTemplate(String hostelId, String type) {

        if (type.equalsIgnoreCase(InvoiceType.RENT.name())) {
            type = BillConfigTypes.RENTAL.name();
        }

        BillTemplates tmp = billTemplatesRepository.getByHostelId(hostelId);
        if (tmp == null) {
            return null;
        }

        String finalType = type;
        List<BillTemplateType> templateType = tmp.getTemplateTypes()
                .stream()
                .filter(item -> item.getInvoiceType().equalsIgnoreCase(finalType))
                .toList();

        if (!templateType.isEmpty()) {
            return new BillTemplatesDto(
                    templateType.getFirst().getInvoicePrefix(),
                    templateType.getFirst().getInvoiceSuffix(),
                    templateType.getFirst().getGstPercentage()
            );
        }

        return null;
    }
}