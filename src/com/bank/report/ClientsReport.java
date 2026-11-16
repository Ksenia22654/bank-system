package com.bank.report;

import com.bank.model.Client;

import java.util.ArrayList;
import java.util.List;

public class ClientsReport extends TextReport {
    private final List<Client> clients;

    public ClientsReport(List<Client> clients) {
        this.clients = clients;
    }

    @Override
    protected String getTitle() {
        return "Отчёт: клиенты банка";
    }

    @Override
    protected String[] getHeaders() {
        return new String[]{"№", "ФИО", "Дата рождения", "Паспорт", "Телефон", "Адрес"};
    }

    @Override
    protected List<String[]> getRows() {
        List<String[]> rows = new ArrayList<>();
        for (Client c : clients) {
            rows.add(new String[]{String.valueOf(c.getId()), c.getFullName(), date(c.getBirthDate()),
                    c.getPassportNo(), c.getPhone(), c.getAddress()});
        }
        return rows;
    }

    @Override
    public String getFileBaseName() {
        return "clients";
    }
}
