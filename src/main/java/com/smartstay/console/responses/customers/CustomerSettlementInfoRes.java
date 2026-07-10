package com.smartstay.console.responses.customers;

public record CustomerSettlementInfoRes(CustomerInfoRes customerInfo,
                                        CustomerStayInfoRes customerStayInfo,
                                        CustomerEbInfoRes customerEbInfo,
                                        UnpaidInvoicesInfoRes unpaidInvoicesInfo,
                                        CustomerRentInfoRes customerRentInfo,
                                        CustomerWalletInfoRes customerWalletInfo,
                                        CustomerBookingInfoRes customerBookingInfo,
                                        CustomerAdvanceInfoRes customerAdvanceInfo,
                                        CustomerDeductionsInfoRes customerDeductionsInfo,
                                        CustomerFinalSettlementInfoRes customerFinalSettlementInfo) {
}
