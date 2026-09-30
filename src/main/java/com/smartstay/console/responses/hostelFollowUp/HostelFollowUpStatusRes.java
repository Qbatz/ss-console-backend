package com.smartstay.console.responses.hostelFollowUp;

import java.util.List;

public record HostelFollowUpStatusRes(String key,
                                      String value,
                                      List<String> reasons) {
}
