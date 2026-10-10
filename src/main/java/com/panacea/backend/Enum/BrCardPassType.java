package com.panacea.backend.Enum;

import java.math.BigDecimal;

public enum BrCardPassType {

    WITHOUT_ACCOMMODATION(BigDecimal.valueOf(1000)),
    WITH_ACCOMMODATION(BigDecimal.valueOf(1500));


    private final BigDecimal amount;

    BrCardPassType(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
