package com.smartstay.console.responses.demoRequest;

import java.util.List;

public record DemoRequestStatusFlowResponse(String currentStatus,
                                            List<DemoRequestStatusResponse> allowedStatuses) {
}
