package com.smartstay.console.Mapper.hostels;

import com.smartstay.console.dao.Agent;
import com.smartstay.console.dao.HostelV1;
import com.smartstay.console.dao.RecurringTracker;
import com.smartstay.console.responses.hostels.RecurringHistoryRes;
import com.smartstay.console.utils.Utils;

import java.util.Date;
import java.util.Map;
import java.util.function.Function;

public class RecurringHistoryMapper implements Function<RecurringTracker, RecurringHistoryRes> {

    HostelV1 hostel;
    Map<String, Agent> agentMap;
    Long invoiceCount;

    public RecurringHistoryMapper(HostelV1 hostel,
                                  Map<String, Agent> agentMap,
                                  Long invoiceCount) {
        this.hostel = hostel;
        this.agentMap = agentMap;
        this.invoiceCount = invoiceCount;
    }

    @Override
    public RecurringHistoryRes apply(RecurringTracker recurringTracker) {

        String hostelName = null;
        if(hostel != null){
            hostelName = hostel.getHostelName();
        }

        String createdBy = null;
        if (agentMap != null){
            Agent agent = agentMap.get(recurringTracker.getCreatedBy());
            if (agent != null){
                createdBy = Utils.getFullName(agent.getFirstName(), agent.getLastName());
            }
        }

        int month = recurringTracker.getCreationMonth();
        int year = recurringTracker.getCreationYear();

        int startDay = recurringTracker.getCreationDay();
        int endDay = Utils.getEndDay(startDay, month, year);

        Date startDate = Utils.getDateFromDay(startDay, month, year);
        Date endDate = Utils.getEndDate(startDay, month, year);

        long invoiceGeneratedCount = invoiceCount;

        return new RecurringHistoryRes(recurringTracker.getTrackerId(), hostelName, recurringTracker.getCreationDay(),
                recurringTracker.getCreationMonth(), recurringTracker.getCreationYear(), startDay, endDay,
                Utils.dateToString(startDate), Utils.dateToString(endDate), invoiceGeneratedCount, recurringTracker.getMode(),
                createdBy, Utils.dateToString(recurringTracker.getCreatedAt()), Utils.dateToTime(recurringTracker.getCreatedAt()));
    }
}
