package com.bank.report;

import com.bank.model.Loan;

import java.util.ArrayList;
import java.util.List;

public class LoansReport extends TextReport {
    private final List<Loan> loans;

    public LoansReport(List<Loan> loans) {
        this.loans = loans;
    }

    @Override
    protected String getTitle() {
        return "Отчёт: кредиты";
    }

    @Override
    protected String[] getHeaders() {
        return new String[]{"№", "Клиент", "Сумма", "Ставка, %", "Срок, мес.", "Платёж в месяц", "Остаток долга", "Статус"};
    }

    @Override
    protected List<String[]> getRows() {
        List<String[]> rows = new ArrayList<>();
        for (Loan l : loans) {
            rows.add(new String[]{String.valueOf(l.getId()), l.getClientName(), money(l.getAmount(), l.getCurrencyCode()),
                    l.getAnnualRate().toPlainString(), String.valueOf(l.getTermMonths()),
                    money(l.getMonthlyPayment(), l.getCurrencyCode()), money(l.getRemaining(), l.getCurrencyCode()),
                    l.getStatusTitle()});
        }
        return rows;
    }

    @Override
    public String getFileBaseName() {
        return "loans";
    }
}
