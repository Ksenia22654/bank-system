package com.bank.report;

import com.bank.model.Account;
import com.bank.model.Operation;

import java.util.List;

/** Выписка по одному счёту: те же столбцы, что у отчёта по операциям, но свой заголовок и итог. */
public class AccountStatementReport extends OperationsReport {
    private final Account account;

    public AccountStatementReport(Account account, List<Operation> operations) {
        super(operations);
        this.account = account;
    }

    @Override
    protected String getTitle() {
        return "Выписка по счёту " + account.getAccountNo() + " (" + account.getClientName() + ")";
    }

    @Override
    protected String getFooter() {
        return "Текущий остаток: " + money(account.getBalance(), account.getCurrencyCode());
    }

    @Override
    public String getFileBaseName() {
        return "statement-" + account.getAccountNo();
    }
}
