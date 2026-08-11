package com.smartstay.console.responses.customers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.smartstay.console.dao.InvoicesV1;

import java.util.List;

public record CustomerRetainerInfoRes(int noOfRetainerInvoices,
                                      Double totalRetainerAmount,
                                      Double totalBalanceAmount,
                                      List<RetainerInfoRes> retainerInfos,
                                      @JsonIgnore
                                      List<InvoicesV1> retainerInvoices) {
}
