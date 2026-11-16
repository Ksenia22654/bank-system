package com.bank.report;

import com.bank.model.Account;

import java.util.ArrayList;
import java.util.List;

public class AccountsReport extends TextReport {
    private final List<Account> accounts;

    public AccountsReport(List<Account> accounts) {
        this.accounts = accounts;
    }

    @Override
    protected String getTitle() {
        return "Отчёт: банковские счета";
    }

    @Override
    protected String[] getHeaders() {
        return new String[]{"Номер счёта", "Клиент", "Вид", "Остаток", "Открыт", "Закрыт"};
    }

    @Override
    protected List<String[]> getRows() {
        List<String[]> rows = new ArrayList<>();
        for (Account a : accounts) {
            rows.add(new String[]{a.getAccountNo(), a.getClientName(), a.getTypeName(),
                    money(a.getBalance(), a.getCurrencyCode()), date(a.getOpenedOn()), date(a.getClosedOn())});
        }
        return rows;
    }

    @Override
    public String getFileBaseName() {
        return "accounts";
    }
}
