package com.smartstay.console.Mapper.users;

import com.smartstay.console.dao.Address;
import com.smartstay.console.dao.HostelV1;
import com.smartstay.console.dao.TableColumns;
import com.smartstay.console.dao.Users;
import com.smartstay.console.responses.tableColumns.TableColumnsResponse;
import com.smartstay.console.responses.users.AddressResponse;
import com.smartstay.console.responses.users.UsersResponse;
import com.smartstay.console.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class UsersResponseMapper implements Function<Users, UsersResponse> {

    List<TableColumns> tableColumns;
    HostelV1 hostel;

    public UsersResponseMapper(List<TableColumns> tableColumns,
                               HostelV1 hostel) {
        this.tableColumns = tableColumns;
        this.hostel = hostel;
    }

    @Override
    public UsersResponse apply(Users users) {

        String hostelName;
        if (hostel != null){
            hostelName = hostel.getHostelName();
        } else {
            hostelName = null;
        }

        StringBuilder fullName = new StringBuilder();
        StringBuilder initials = new StringBuilder();
        String firstName = null;
        String lastName = null;

        if (users.getFirstName() != null) {
            firstName = users.getFirstName().trim();
            fullName.append(firstName);
            initials.append(firstName.toUpperCase().charAt(0));
        }

        if (users.getLastName() != null && !users.getLastName().isBlank()) {
            lastName = users.getLastName().trim();
            fullName.append(" ");
            fullName.append(lastName);
            initials.append(lastName.toUpperCase().charAt(0));
        }
        else {
            if (firstName != null) {
                String[] nameArr = firstName.split(" ");
                if (nameArr.length > 1) {
                    initials.append(nameArr[nameArr.length - 1].toUpperCase().charAt(0));
                }
                else {
                    String lastPart = nameArr[nameArr.length - 1].toUpperCase();

                    if (lastPart.length() > 1) {
                        initials.append(lastPart.charAt(1));
                    }
                }
            }
        }

        AddressResponse addressRes = null;
        Address address = users.getAddress();

        if (address != null){
            addressRes = new AddressResponse(address.getAddressId(), address.getHouseNo(), address.getStreet(),
                    address.getLandMark(), address.getCity(), address.getState(), address.getPincode());
        }

        List<TableColumnsResponse> tableColumnsRes = new ArrayList<>();
        if (tableColumns != null && !tableColumns.isEmpty()) {
            tableColumnsRes = tableColumns.stream()
                    .map(tableColumn -> new TableColumnsResponse(tableColumn.getColumnId(), tableColumn.getHostelId(),
                            hostelName, tableColumn.getUserId(), fullName.toString(), tableColumn.getModuleName(), tableColumn.getColumns(),
                            Utils.dateToString(tableColumn.getCreatedAt()), Utils.dateToTime(tableColumn.getCreatedAt()),
                            Utils.dateToString(tableColumn.getUpdatedAt()), Utils.dateToTime(tableColumn.getUpdatedAt())))
                    .toList();
        }

        String lastUpdateDate = null;
        String lastUpdateTime = null;
        if (users.getLastUpdate() != null) {
            lastUpdateDate = Utils.dateToString(users.getLastUpdate());
            lastUpdateTime = Utils.dateToTime(users.getLastUpdate());
        }

        return new UsersResponse(users.getUserId(), users.getParentId(), firstName,
                lastName, fullName.toString(), initials.toString(), users.getMobileNo(),
                users.getEmailId(), lastUpdateDate, lastUpdateTime, addressRes, tableColumnsRes);
    }
}
