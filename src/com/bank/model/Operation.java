package com.bank.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/** Банковская операция: списание со счёта, зачисление на счёт или то и другое. */
public class Operation extends BaseEntity {
    private static final long serialVersionUID = 1L;

    public static final String DEPOSIT = "DEPOSIT";
    public static final String WITHDRAW = "WITHDRAW";
    public static final String TRANSFER = "TRANSFER";
    public static final String INTEREST = "INTEREST";
    public static final String LOAN_ISSUE = "LOAN_ISSUE";
    public static final String LOAN_PAYMENT = "LOAN_PAYMENT";

    private String typeCode;
    private Integer fromAccountId;
    private Integer toAccountId;
    private BigDecimal debitAmount;
    private BigDecimal creditAmount;
    private Integer loanId;
    private Timestamp performedAt;
    private Integer performedBy;
    private String description;

    private String typeTitle;
    private String fromAccountNo;
    private String toAccountNo;
    private String fromCurrency;
    private String toCurrency;
    private String performerLogin;

    @Override
    public String getTitle() {
        return typeTitle != null ? typeTitle : typeCode;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public Integer getFromAccountId() {
        return fromAccountId;
    }

    public void setFromAccountId(Integer fromAccountId) {
        this.fromAccountId = fromAccountId;
    }

    public Integer getToAccountId() {
        return toAccountId;
    }

    public void setToAccountId(Integer toAccountId) {
        this.toAccountId = toAccountId;
    }

    public BigDecimal getDebitAmount() {
        return debitAmount;
    }

    public void setDebitAmount(BigDecimal debitAmount) {
        this.debitAmount = debitAmount;
    }

    public BigDecimal getCreditAmount() {
        return creditAmount;
    }

    public void setCreditAmount(BigDecimal creditAmount) {
        this.creditAmount = creditAmount;
    }

    public Integer getLoanId() {
        return loanId;
    }

    public void setLoanId(Integer loanId) {
        this.loanId = loanId;
    }

    public Timestamp getPerformedAt() {
        return performedAt;
    }

    public void setPerformedAt(Timestamp performedAt) {
        this.performedAt = performedAt;
    }

    public Integer getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(Integer performedBy) {
        this.performedBy = performedBy;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTypeTitle() {
        return typeTitle;
    }

    public void setTypeTitle(String typeTitle) {
        this.typeTitle = typeTitle;
    }

    public String getFromAccountNo() {
        return fromAccountNo;
    }

    public void setFromAccountNo(String fromAccountNo) {
        this.fromAccountNo = fromAccountNo;
    }

    public String getToAccountNo() {
        return toAccountNo;
    }

    public void setToAccountNo(String toAccountNo) {
        this.toAccountNo = toAccountNo;
    }

    public String getFromCurrency() {
        return fromCurrency;
    }

    public void setFromCurrency(String fromCurrency) {
        this.fromCurrency = fromCurrency;
    }

    public String getToCurrency() {
        return toCurrency;
    }

    public void setToCurrency(String toCurrency) {
        this.toCurrency = toCurrency;
    }

    public String getPerformerLogin() {
        return performerLogin;
    }

    public void setPerformerLogin(String performerLogin) {
        this.performerLogin = performerLogin;
    }
}
