package com.bank.report;

import com.bank.model.Operation;

import java.util.ArrayList;
import java.util.List;

public class OperationsReport extends TextReport {
    private final List<Operation> operations;

    public OperationsReport(List<Operation> operations) {
        this.operations = operations;
    }

    @Override
    protected String getTitle() {
        return "Отчёт: банковские операции";
    }

    @Override
    protected String[] getHeaders() {
        return new String[]{"Дата и время", "Вид операции", "Со счёта", "Списано", "На счёт", "Зачислено", "Назначение"};
    }

    @Override
    protected List<String[]> getRows() {
        List<String[]> rows = new ArrayList<>();
        for (Operation o : operations) {
            rows.add(new String[]{date(o.getPerformedAt(), "dd.MM.yyyy HH:mm"), o.getTypeTitle(),
                    text(o.getFromAccountNo()), o.getDebitAmount() == null ? "—" : money(o.getDebitAmount(), o.getFromCurrency()),
                    text(o.getToAccountNo()), o.getCreditAmount() == null ? "—" : money(o.getCreditAmount(), o.getToCurrency()),
                    text(o.getDescription())});
        }
        return rows;
    }

    @Override
    public String getFileBaseName() {
        return "operations";
    }
}
