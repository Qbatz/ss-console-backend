package com.smartstay.console.responses.hostels;

public record SharingTypeResponse(Integer sharingType,
                                  String sharingTypeDisplay,
                                  int noOfRooms,
                                  int noOfBeds,
                                  int noOfOccupiedBeds,
                                  int noOfRoomsAvailable) {
}
