package com.bank.model;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Date;

/** Кредит физического лица. Клиент и валюта определяются через связанный счёт. */
public class Loan extends BaseEntity {
    private static final long serialVersionUID = 1L;

    public static final String PENDING = "PENDING";
    public static final String ACTIVE = "ACTIVE";
    public static final String REJECTED = "REJECTED";
    public static final String CLOSED = "CLOSED";

    private int accountId;
    private BigDecimal amount;
    private BigDecimal annualRate;
    private int termMonths;
    private Date appliedOn;
    private Date issuedOn;
    private String statusCode;

    private String statusTitle;
    private String accountNo;
    private int clientId;
    private String clientName;
    private String currencyCode;
    private BigDecimal paidAmount = BigDecimal.ZERO;

    /**
     * Аннуитетный ежемесячный платёж: A = S * i / (1 - (1 + i)^(-n)),
     * где i — месячная ставка, n — срок в месяцах.
     */
    public static BigDecimal annuityPayment(BigDecimal amount, BigDecimal annualRate, int months) {
        if (annualRate.signum() == 0) {
            return amount.divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);
        }
        MathContext mc = MathContext.DECIMAL64;
        BigDecimal monthly = annualRate.divide(BigDecimal.valueOf(1200), mc);
        BigDecimal growth = BigDecimal.ONE.add(monthly).pow(months, mc);
        BigDecimal factor = BigDecimal.ONE.subtract(BigDecimal.ONE.divide(growth, mc));
        return amount.multiply(monthly, mc).divide(factor, 2, RoundingMode.HALF_UP);
    }

    public BigDecimal getMonthlyPayment() {
        return annuityPayment(amount, annualRate, termMonths);
    }

    public BigDecimal getTotalDue() {
        return getMonthlyPayment().multiply(BigDecimal.valueOf(termMonths));
    }

    public BigDecimal getRemaining() {
        return getTotalDue().subtract(paidAmount).max(BigDecimal.ZERO);
    }

    public boolean hasStatus(String code) {
        return code.equals(statusCode);
    }

    @Override
    public String getTitle() {
        return "Кредит №" + getId();
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAnnualRate() {
        return annualRate;
    }

    public void setAnnualRate(BigDecimal annualRate) {
        this.annualRate = annualRate;
    }

    public int getTermMonths() {
        return termMonths;
    }

    public void setTermMonths(int termMonths) {
        this.termMonths = termMonths;
    }

    public Date getAppliedOn() {
        return appliedOn;
    }

    public void setAppliedOn(Date appliedOn) {
        this.appliedOn = appliedOn;
    }

    public Date getIssuedOn() {
        return issuedOn;
    }

    public void setIssuedOn(Date issuedOn) {
        this.issuedOn = issuedOn;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusTitle() {
        return statusTitle;
    }

    public void setStatusTitle(String statusTitle) {
        this.statusTitle = statusTitle;
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

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
    }
}
