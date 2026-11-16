package com.bank.controller;

import com.bank.model.User;
import com.bank.service.AccountService;
import com.bank.service.AnalyticsService;
import com.bank.service.OperationService;

/** Главная страница со сводкой. */
public class HomeModule extends CommandModule {
    private final AnalyticsService analytics = new AnalyticsService();
    private final OperationService operations = new OperationService();
    private final AccountService accounts = new AccountService();

    @Override
    public void register(CommandRegistry r) {
        r.get("/", (req, resp) -> View.redirect("/home"));
        r.get("/home", (req, resp) -> {
            User user = currentUser(req);
            req.setAttribute("summary", analytics.summary(user));
            req.setAttribute("operations", operations.recentFor(user, 8));
            if (!user.isAdmin()) {
                req.setAttribute("accounts", accounts.listFor(user, null));
            }
            return View.jsp("home");
        });
    }
}
