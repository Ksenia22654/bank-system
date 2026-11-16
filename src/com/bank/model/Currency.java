package com.bank.model;

import java.math.BigDecimal;

/** Валюта и её курс к базовой валюте банка. */
public class Currency extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private String code;
    private String name;
    private BigDecimal rate;

    @Override
    public String getTitle() {
        return code + " — " + name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }
}
