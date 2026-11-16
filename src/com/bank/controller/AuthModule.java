package com.bank.controller;

import com.bank.exception.BankException;
import com.bank.exception.ValidationException;
import com.bank.model.User;
import com.bank.service.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/** Вход, выход и смена собственного пароля. */
public class AuthModule extends CommandModule {
    private final UserService users = new UserService();

    @Override
    public void register(CommandRegistry r) {
        r.get("/login", (req, resp) -> View.jsp("login"));
        r.post("/login", this::login);
        r.post("/logout", this::logout);
        r.get("/profile", (req, resp) -> View.jsp("profile"));
        r.post("/profile", this::changePassword);
    }

    private View login(HttpServletRequest request, HttpServletResponse response) {
        User user = users.authenticate(request.getParameter("login"), request.getParameter("password"));
        if (user == null) {
            request.setAttribute("loginError", "Неверный логин или пароль");
            request.setAttribute("enteredLogin", request.getParameter("login"));
            return View.jsp("login");
        }
        // Новая сессия после входа защищает от фиксации идентификатора сессии.
        HttpSession old = request.getSession(false);
        if (old != null) {
            old.invalidate();
        }
        request.getSession(true).setAttribute("userId", user.getId());
        return View.redirect("/home");
    }

    private View logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return View.redirect("/login");
    }

    private View changePassword(HttpServletRequest request, HttpServletResponse response) throws BankException {
        try {
            users.changePassword(currentUser(request), request.getParameter("current"),
                    request.getParameter("next"), request.getParameter("repeat"));
        } catch (ValidationException e) {
            showErrors(request, e);
            return View.jsp("profile");
        }
        Flash.success(request, "Пароль изменён");
        return View.redirect("/profile");
    }
}
