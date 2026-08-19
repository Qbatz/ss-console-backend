package com.smartstay.console.responses.beds;

import java.util.List;

public record RoomsResponse(int roomId,
                            String roomName,
                            List<BedsResponse> beds) {
}
