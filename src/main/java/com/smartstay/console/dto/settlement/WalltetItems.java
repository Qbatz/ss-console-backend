package com.smartstay.console.dto.settlement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WalltetItems {
    private String type;
    private Double amount;
    private Long walletId;
}
