package com.bank.model;

import java.math.BigDecimal;

/** Вид счёта и годовая процентная ставка по нему. */
public class AccountType extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private String name;
    private BigDecimal annualRate;

    @Override
    public String getTitle() {
        return name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getAnnualRate() {
        return annualRate;
    }

    public void setAnnualRate(BigDecimal annualRate) {
        this.annualRate = annualRate;
    }
}
