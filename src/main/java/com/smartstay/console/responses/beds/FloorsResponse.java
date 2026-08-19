package com.smartstay.console.responses.beds;

import java.util.List;

public record FloorsResponse(int floorId,
                             String floorName,
                             List<RoomsResponse> rooms) {
}
