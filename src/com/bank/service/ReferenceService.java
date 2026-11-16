package com.bank.service;

import com.bank.dao.AccountTypeDao;
import com.bank.dao.CurrencyDao;
import com.bank.exception.NotFoundException;
import com.bank.exception.ValidationException;
import com.bank.model.AccountType;
import com.bank.model.Currency;
import com.bank.util.Validator;

import java.math.BigDecimal;
import java.util.List;

/** Справочники: валюты и виды счетов. */
public class ReferenceService {
    private final CurrencyDao currencies = new CurrencyDao();
    private final AccountTypeDao types = new AccountTypeDao();

    public List<Currency> currencies() {
        return currencies.findAll();
    }

    public List<AccountType> accountTypes() {
        return types.findAll();
    }

    public Currency currency(int id) throws NotFoundException {
        Currency currency = currencies.findById(id);
        if (currency == null) {
            throw new NotFoundException("Валюта не найдена");
        }
        return currency;
    }

    public AccountType accountType(int id) throws NotFoundException {
        AccountType type = types.findById(id);
        if (type == null) {
            throw new NotFoundException("Вид счёта не найден");
        }
        return type;
    }

    public void save(Currency c) throws ValidationException {
        Validator v = new Validator();
        v.required("code", c.getCode(), "Код").pattern("code", c.getCode(), "[A-Z]{3}", "Код валюты: три латинские заглавные буквы");
        v.required("name", c.getName(), "Название").maxLength("name", c.getName(), 50, "Название");
        v.range("rate", c.getRate(), new BigDecimal("0.0001"), new BigDecimal("99999999"), "Курс");
        v.throwIfInvalid();
        currencies.save(c);
    }

    public void save(AccountType t) throws ValidationException {
        Validator v = new Validator();
        v.required("name", t.getName(), "Название").maxLength("name", t.getName(), 50, "Название");
        v.range("annualRate", t.getAnnualRate(), BigDecimal.ZERO, new BigDecimal("100"), "Ставка");
        v.throwIfInvalid();
        types.save(t);
    }

    public void deleteCurrency(int id) {
        currencies.delete(id);
    }

    public void deleteAccountType(int id) {
        types.delete(id);
    }
}
