package com.smartstay.console.responses.customers;

import java.util.List;

public record CustomerRetainerInfoRes(int noOfRetainerInvoices,
                                      Double totalRetainerAmount,
                                      Double totalBalanceAmount,
                                      List<RetainerInfoRes> retainerInfos) {
}
