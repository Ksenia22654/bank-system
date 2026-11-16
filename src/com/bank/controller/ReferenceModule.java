package com.bank.controller;

import com.bank.exception.BankException;
import com.bank.exception.ValidationException;
import com.bank.model.AccountType;
import com.bank.model.Currency;
import com.bank.model.User;
import com.bank.service.ReferenceService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Справочники валют и видов счетов. Список и форма находятся на одной странице. */
public class ReferenceModule extends CommandModule {
    private final ReferenceService references = new ReferenceService();

    @Override
    public void register(CommandRegistry r) {
        r.get("/currencies", User.ADMIN, this::currencies);
        r.post("/currencies/save", User.ADMIN, this::saveCurrency);
        r.post("/currencies/delete", User.ADMIN, (req, resp) -> {
            references.deleteCurrency(id(req));
            Flash.success(req, "Валюта удалена");
            return View.redirect("/currencies");
        });
        r.get("/account-types", User.ADMIN, this::types);
        r.post("/account-types/save", User.ADMIN, this::saveType);
        r.post("/account-types/delete", User.ADMIN, (req, resp) -> {
            references.deleteAccountType(id(req));
            Flash.success(req, "Вид счёта удалён");
            return View.redirect("/account-types");
        });
    }

    private View currencies(HttpServletRequest request, HttpServletResponse response) throws BankException {
        Integer id = integer(request, "id");
        request.setAttribute("item", id == null ? new Currency() : references.currency(id));
        request.setAttribute("items", references.currencies());
        return View.jsp("refs/currencies");
    }

    private View saveCurrency(HttpServletRequest request, HttpServletResponse response) {
        Currency currency = new Currency();
        Integer id = integer(request, "id");
        currency.setId(id == null ? 0 : id);
        currency.setCode(text(request, "code"));
        currency.setName(text(request, "name"));
        currency.setRate(decimal(request, "rate"));
        try {
            references.save(currency);
        } catch (ValidationException e) {
            showErrors(request, e);
            request.setAttribute("item", currency);
            request.setAttribute("items", references.currencies());
            return View.jsp("refs/currencies");
        }
        Flash.success(request, "Валюта сохранена");
        return View.redirect("/currencies");
    }

    private View types(HttpServletRequest request, HttpServletResponse response) throws BankException {
        Integer id = integer(request, "id");
        request.setAttribute("item", id == null ? new AccountType() : references.accountType(id));
        request.setAttribute("items", references.accountTypes());
        return View.jsp("refs/types");
    }

    private View saveType(HttpServletRequest request, HttpServletResponse response) {
        AccountType type = new AccountType();
        Integer id = integer(request, "id");
        type.setId(id == null ? 0 : id);
        type.setName(text(request, "name"));
        type.setAnnualRate(decimal(request, "annualRate"));
        try {
            references.save(type);
        } catch (ValidationException e) {
            showErrors(request, e);
            request.setAttribute("item", type);
            request.setAttribute("items", references.accountTypes());
            return View.jsp("refs/types");
        }
        Flash.success(request, "Вид счёта сохранён");
        return View.redirect("/account-types");
    }
}
