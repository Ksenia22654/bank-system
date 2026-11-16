package com.bank.controller;

import com.bank.exception.BankException;
import com.bank.exception.ValidationException;
import com.bank.model.Account;
import com.bank.model.User;
import com.bank.service.AccountService;
import com.bank.service.ClientService;
import com.bank.service.OperationService;
import com.bank.service.ReferenceService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Счета: список, открытие, выписка, закрытие и удаление. */
public class AccountModule extends CommandModule {
    private final AccountService accounts = new AccountService();
    private final ClientService clients = new ClientService();
    private final ReferenceService references = new ReferenceService();
    private final OperationService operations = new OperationService();

    @Override
    public void register(CommandRegistry r) {
        r.get("/accounts", this::list);
        r.get("/accounts/view", this::view);
        r.get("/accounts/new", this::form);
        r.post("/accounts/open", this::open);
        r.post("/accounts/close", this::close);
        r.post("/accounts/delete", User.ADMIN, this::delete);
    }

    private View list(HttpServletRequest request, HttpServletResponse response) {
        request.setAttribute("accounts", accounts.listFor(currentUser(request), request.getParameter("q")));
        request.setAttribute("q", request.getParameter("q"));
        return View.jsp("accounts/list");
    }

    private View view(HttpServletRequest request, HttpServletResponse response) throws BankException {
        Account account = accounts.get(currentUser(request), id(request));
        request.setAttribute("account", account);
        request.setAttribute("operations", operations.forAccount(account.getId()));
        return View.jsp("accounts/view");
    }

    private View form(HttpServletRequest request, HttpServletResponse response) {
        prepareForm(request);
        request.setAttribute("selectedClientId", integer(request, "clientId"));
        return View.jsp("accounts/form");
    }

    private void prepareForm(HttpServletRequest request) {
        if (currentUser(request).isAdmin()) {
            request.setAttribute("clients", clients.search(null));
        }
        request.setAttribute("types", references.accountTypes());
        request.setAttribute("currencies", references.currencies());
    }

    private View open(HttpServletRequest request, HttpServletResponse response) throws BankException {
        User user = currentUser(request);
        Integer clientId = user.isAdmin() ? integer(request, "clientId") : user.getClientId();
        Account account;
        try {
            account = accounts.open(user, clientId, integer(request, "typeId"), integer(request, "currencyId"));
        } catch (ValidationException e) {
            showErrors(request, e);
            prepareForm(request);
            return View.jsp("accounts/form");
        }
        Flash.success(request, "Открыт счёт " + account.getAccountNo());
        return View.redirect("/accounts/view?id=" + account.getId());
    }

    private View close(HttpServletRequest request, HttpServletResponse response) throws BankException {
        int id = id(request);
        accounts.close(currentUser(request), id);
        Flash.success(request, "Счёт закрыт");
        return View.redirect("/accounts/view?id=" + id);
    }

    private View delete(HttpServletRequest request, HttpServletResponse response) throws BankException {
        accounts.delete(id(request));
        Flash.success(request, "Счёт удалён");
        return View.redirect("/accounts");
    }
}
