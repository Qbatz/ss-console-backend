package com.smartstay.console.responses.users;

import com.smartstay.console.responses.tableColumns.TableColumnsResponse;

import java.util.List;

public record UsersResponse(String userId,
                            String parentId,
                            String firstName,
                            String lastName,
                            String fullName,
                            String initials,
                            String mobileNo,
                            String emailId,
                            String lastUpdateDate,
                            String lastUpdateTime,
                            AddressResponse address,
                            List<TableColumnsResponse> tableColumns) {
}
