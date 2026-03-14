package com.smartstay.console.Mapper.demoRequests;

import com.smartstay.console.dao.DemoRequest;
import com.smartstay.console.responses.demoRequest.DemoRequestResponse;
import com.smartstay.console.utils.Utils;

import java.util.function.Function;

public class DemoRequestMapper implements Function<DemoRequest, DemoRequestResponse> {

    @Override
    public DemoRequestResponse apply(DemoRequest demoRequest) {

        return new DemoRequestResponse(demoRequest.getRequestId(), demoRequest.getName(),
                demoRequest.getEmailId(), demoRequest.getContactNo(), demoRequest.getCountryCode(),
                demoRequest.getOrganization(), demoRequest.getNoOfHostels(), demoRequest.getNoOfTenant(),
                demoRequest.getCity(), demoRequest.getState(), demoRequest.getCountry(), demoRequest.getDemoRequestStatus(),
                demoRequest.getIsDemoCompleted(), demoRequest.getIsAssigned(), demoRequest.getAssignedTo(),
                demoRequest.getAssignedBy(), demoRequest.getPresentedBy(), demoRequest.getComments(),
                demoRequest.getRequestedDate(), demoRequest.getRequestedTime(), Utils.dateToString(demoRequest.getPresentedAt()),
                Utils.dateToTime(demoRequest.getPresentedAt()));
    }
}
