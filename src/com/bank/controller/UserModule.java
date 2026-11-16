package com.bank.controller;

import com.bank.exception.BankException;
import com.bank.exception.ValidationException;
import com.bank.model.User;
import com.bank.service.ClientService;
import com.bank.service.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Учётные записи: управление доступно только администратору. */
public class UserModule extends CommandModule {
    private final UserService users = new UserService();
    private final ClientService clients = new ClientService();

    @Override
    public void register(CommandRegistry r) {
        r.get("/users", User.ADMIN, (req, resp) -> {
            req.setAttribute("users", users.list());
            return View.jsp("users/list");
        });
        r.get("/users/new", User.ADMIN, (req, resp) -> {
            User user = new User();
            user.setRoleCode(User.USER);
            return form(req, user);
        });
        r.get("/users/edit", User.ADMIN, (req, resp) -> form(req, users.get(id(req))));
        r.post("/users/save", User.ADMIN, this::save);
        r.post("/users/delete", User.ADMIN, this::delete);
    }

    private View form(HttpServletRequest request, User user) {
        request.setAttribute("user", user);
        request.setAttribute("clients", clients.search(null));
        return View.jsp("users/form");
    }

    private View save(HttpServletRequest request, HttpServletResponse response) throws BankException {
        User user = new User();
        Integer id = integer(request, "id");
        user.setId(id == null ? 0 : id);
        user.setLogin(text(request, "login"));
        user.setRoleCode(text(request, "roleCode"));
        user.setClientId(integer(request, "clientId"));
        user.setActive(request.getParameter("active") != null);
        try {
            users.save(user, request.getParameter("password"));
        } catch (ValidationException e) {
            showErrors(request, e);
            return form(request, user);
        }
        Flash.success(request, "Учётная запись сохранена");
        return View.redirect("/users");
    }

    private View delete(HttpServletRequest request, HttpServletResponse response) throws BankException {
        users.delete(currentUser(request), id(request));
        Flash.success(request, "Учётная запись удалена");
        return View.redirect("/users");
    }
}
