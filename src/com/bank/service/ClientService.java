package com.bank.service;

import com.bank.dao.AccountDao;
import com.bank.dao.ClientDao;
import com.bank.exception.BankException;
import com.bank.exception.BusinessRuleException;
import com.bank.exception.NotFoundException;
import com.bank.exception.ValidationException;
import com.bank.model.Client;
import com.bank.util.Validator;

import java.sql.Date;
import java.util.List;

public class ClientService {
    private final ClientDao clients = new ClientDao();
    private final AccountDao accounts = new AccountDao();

    public List<Client> search(String text) {
        return text == null || text.trim().isEmpty() ? clients.findAll() : clients.search(text.trim());
    }

    public Client get(int id) throws NotFoundException {
        Client client = clients.findById(id);
        if (client == null) {
            throw new NotFoundException("Клиент не найден");
        }
        return client;
    }

    public Client save(Client c) throws ValidationException {
        Validator v = new Validator();
        v.required("lastName", c.getLastName(), "Фамилия").maxLength("lastName", c.getLastName(), 50, "Фамилия");
        v.required("firstName", c.getFirstName(), "Имя").maxLength("firstName", c.getFirstName(), 50, "Имя");
        v.maxLength("middleName", c.getMiddleName(), 50, "Отчество");
        v.required("birthDate", c.getBirthDate(), "Дата рождения").notFuture("birthDate", c.getBirthDate(), "Дата рождения");
        v.required("passportNo", c.getPassportNo(), "Паспорт")
                .pattern("passportNo", c.getPassportNo(), "[0-9A-ZА-ЯЁ ]{6,20}", "Паспорт: от 6 до 20 цифр и заглавных букв");
        v.required("phone", c.getPhone(), "Телефон")
                .pattern("phone", c.getPhone(), "\\+?[0-9 ()-]{7,20}", "Телефон: от 7 до 20 цифр, допустимы + ( ) -");
        v.pattern("email", c.getEmail(), "[^@\\s]+@[^@\\s]+\\.[^@\\s]+", "Укажите корректный адрес электронной почты");
        v.maxLength("email", c.getEmail(), 100, "Эл. почта");
        v.required("address", c.getAddress(), "Адрес").maxLength("address", c.getAddress(), 200, "Адрес");
        Client samePassport = c.getPassportNo() == null ? null : clients.findByPassport(c.getPassportNo());
        if (samePassport != null && samePassport.getId() != c.getId()) {
            v.add("passportNo", "Клиент с таким паспортом уже зарегистрирован");
        }
        if (c.getBirthDate() != null && isUnderage(c.getBirthDate())) {
            v.add("birthDate", "Клиенту должно быть не менее 14 лет");
        }
        v.throwIfInvalid();
        if (c.isNew()) {
            c.setRegisteredOn(new Date(System.currentTimeMillis()));
        }
        return clients.save(c);
    }

    public void delete(int id) throws BankException {
        if (!accounts.findByClient(id).isEmpty()) {
            throw new BusinessRuleException("У клиента есть счета. Сначала удалите их");
        }
        if (!clients.delete(id)) {
            throw new NotFoundException("Клиент не найден");
        }
    }

    private static boolean isUnderage(Date birthDate) {
        return birthDate.toLocalDate().plusYears(14).isAfter(java.time.LocalDate.now());
    }
}
