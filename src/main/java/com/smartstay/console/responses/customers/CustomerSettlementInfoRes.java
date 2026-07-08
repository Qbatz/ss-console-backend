package com.smartstay.console.responses.customers;

import java.util.List;

public record CustomerSettlementInfoRes(CustomerInfoRes customerInfo,
                                        CustomerStayInfoRes customerStayInfo,
                                        CustomerEbInfoRes customerEbInfo,
                                        List<UnpaidInvoicesInfoRes> unpaidInvoicesInfo,
                                        CustomerRentInfoRes customerRentInfo,
                                        CustomerWalletInfoRes customerWalletInfo,
                                        CustomerBookingInfoRes customerBookingInfo,
                                        CustomerAdvanceInfoRes customerAdvanceInfo,
                                        CustomerDeductionsInfoRes customerDeductionsInfo,
                                        CustomerFinalSettlementInfoRes customerFinalSettlementInfo) {
}
