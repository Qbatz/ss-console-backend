package com.smartstay.console.dto.hostel;

public record HostelImageSnapshot(Integer hostelImagesId,
                                  String imageUrl,
                                  String createdBy,
                                  String hostelId) {
}
