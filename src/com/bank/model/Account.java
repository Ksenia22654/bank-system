package com.bank.model;

import java.math.BigDecimal;
import java.sql.Date;

/** Банковский счёт клиента. Поля clientName, typeName и currencyCode заполняются из связанных таблиц. */
public class Account extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private String accountNo;
    private int clientId;
    private int typeId;
    private int currencyId;
    private BigDecimal balance = BigDecimal.ZERO;
    private Date openedOn;
    private Date closedOn;

    private String clientName;
    private String typeName;
    private String currencyCode;
    private BigDecimal currencyRate;
    private BigDecimal annualRate;

    public boolean isActive() {
        return closedOn == null;
    }

    public boolean isOwnedBy(Integer ownerClientId) {
        return ownerClientId != null && ownerClientId == clientId;
    }

    @Override
    public String getTitle() {
        return accountNo;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public int getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(int currencyId) {
        this.currencyId = currencyId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Date getOpenedOn() {
        return openedOn;
    }

    public void setOpenedOn(Date openedOn) {
        this.openedOn = openedOn;
    }

    public Date getClosedOn() {
        return closedOn;
    }

    public void setClosedOn(Date closedOn) {
        this.closedOn = closedOn;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public BigDecimal getCurrencyRate() {
        return currencyRate;
    }

    public void setCurrencyRate(BigDecimal currencyRate) {
        this.currencyRate = currencyRate;
    }

    public BigDecimal getAnnualRate() {
        return annualRate;
    }

    public void setAnnualRate(BigDecimal annualRate) {
        this.annualRate = annualRate;
    }
}
