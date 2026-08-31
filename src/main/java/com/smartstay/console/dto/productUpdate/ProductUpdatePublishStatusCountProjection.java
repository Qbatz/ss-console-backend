package com.smartstay.console.dto.productUpdate;

public interface ProductUpdatePublishStatusCountProjection {
    long getTotalCount();
    long getDraftCount();
    long getScheduledCount();
    long getPublishedCount();
}
