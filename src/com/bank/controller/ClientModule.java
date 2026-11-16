package com.bank.controller;

import com.bank.exception.BankException;
import com.bank.exception.ValidationException;
import com.bank.model.Client;
import com.bank.model.User;
import com.bank.service.AccountService;
import com.bank.service.ClientService;
import com.bank.service.LoanService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Клиенты банка: просмотр, поиск, добавление, редактирование и удаление. */
public class ClientModule extends CommandModule {
    private final ClientService clients = new ClientService();
    private final AccountService accounts = new AccountService();
    private final LoanService loans = new LoanService();

    @Override
    public void register(CommandRegistry r) {
        r.get("/clients", User.ADMIN, this::list);
        r.get("/clients/view", User.ADMIN, this::view);
        r.get("/clients/new", User.ADMIN, (req, resp) -> {
            req.setAttribute("client", new Client());
            return View.jsp("clients/form");
        });
        r.get("/clients/edit", User.ADMIN, (req, resp) -> {
            req.setAttribute("client", clients.get(id(req)));
            return View.jsp("clients/form");
        });
        r.post("/clients/save", User.ADMIN, this::save);
        r.post("/clients/delete", User.ADMIN, this::delete);
    }

    private View list(HttpServletRequest request, HttpServletResponse response) {
        request.setAttribute("clients", clients.search(request.getParameter("q")));
        request.setAttribute("q", request.getParameter("q"));
        return View.jsp("clients/list");
    }

    private View view(HttpServletRequest request, HttpServletResponse response) throws BankException {
        int id = id(request);
        request.setAttribute("client", clients.get(id));
        request.setAttribute("accounts", accounts.listByClient(id));
        request.setAttribute("loans", loans.listByClient(id));
        return View.jsp("clients/view");
    }

    private View save(HttpServletRequest request, HttpServletResponse response) {
        Client client = new Client();
        Integer id = integer(request, "id");
        client.setId(id == null ? 0 : id);
        client.setLastName(text(request, "lastName"));
        client.setFirstName(text(request, "firstName"));
        client.setMiddleName(text(request, "middleName"));
        client.setBirthDate(date(request, "birthDate"));
        client.setPassportNo(text(request, "passportNo"));
        client.setPhone(text(request, "phone"));
        client.setEmail(text(request, "email"));
        client.setAddress(text(request, "address"));
        try {
            clients.save(client);
        } catch (ValidationException e) {
            showErrors(request, e);
            request.setAttribute("client", client);
            return View.jsp("clients/form");
        }
        Flash.success(request, "Данные клиента сохранены");
        return View.redirect("/clients/view?id=" + client.getId());
    }

    private View delete(HttpServletRequest request, HttpServletResponse response) throws BankException {
        clients.delete(id(request));
        Flash.success(request, "Клиент удалён");
        return View.redirect("/clients");
    }
}
