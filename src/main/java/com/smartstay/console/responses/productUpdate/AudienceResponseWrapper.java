package com.smartstay.console.responses.productUpdate;

import java.util.List;

public record AudienceResponseWrapper(List<PlanAudienceRes> planAudiences,
                                      List<HostelAudienceRes> hostelAudiences,
                                      List<OwnerAudienceRes> ownerAudiences,
                                      boolean isPlanAudience,
                                      boolean isHostelAudience,
                                      boolean isOwnerAudience) {
}
