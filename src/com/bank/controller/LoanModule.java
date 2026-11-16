package com.bank.controller;

import com.bank.exception.BankException;
import com.bank.exception.ValidationException;
import com.bank.model.User;
import com.bank.service.AccountService;
import com.bank.service.LoanService;
import com.bank.util.AppConfig;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Кредиты: заявка клиента, рассмотрение администратором и платежи. */
public class LoanModule extends CommandModule {
    private final LoanService loans = new LoanService();
    private final AccountService accounts = new AccountService();

    @Override
    public void register(CommandRegistry r) {
        r.get("/loans", (req, resp) -> {
            req.setAttribute("loans", loans.listFor(currentUser(req)));
            return View.jsp("loans/list");
        });
        r.get("/loans/apply", (req, resp) -> applyForm(req));
        r.post("/loans/apply", this::apply);
        r.get("/loans/edit", User.ADMIN, (req, resp) -> {
            req.setAttribute("loan", loans.get(currentUser(req), id(req)));
            return View.jsp("loans/form");
        });
        r.post("/loans/save", User.ADMIN, this::save);
        r.post("/loans/approve", User.ADMIN, this::approve);
        r.post("/loans/reject", User.ADMIN, this::reject);
        r.post("/loans/pay", this::pay);
        r.post("/loans/delete", User.ADMIN, this::delete);
    }

    private View applyForm(HttpServletRequest request) {
        request.setAttribute("accounts", accounts.listFor(currentUser(request), null));
        request.setAttribute("defaultRate", AppConfig.defaultLoanRate());
        return View.jsp("loans/apply");
    }

    private View apply(HttpServletRequest request, HttpServletResponse response) throws BankException {
        try {
            loans.apply(currentUser(request), integer(request, "accountId"), decimal(request, "amount"),
                    integer(request, "termMonths"));
        } catch (ValidationException e) {
            showErrors(request, e);
            return applyForm(request);
        }
        Flash.success(request, "Заявка на кредит отправлена");
        return View.redirect("/loans");
    }

    private View save(HttpServletRequest request, HttpServletResponse response) throws BankException {
        int id = id(request);
        try {
            loans.update(id, decimal(request, "amount"), decimal(request, "annualRate"), integer(request, "termMonths"));
        } catch (ValidationException e) {
            showErrors(request, e);
            request.setAttribute("loan", loans.get(currentUser(request), id));
            return View.jsp("loans/form");
        }
        Flash.success(request, "Заявка изменена");
        return View.redirect("/loans");
    }

    private View approve(HttpServletRequest request, HttpServletResponse response) throws BankException {
        loans.approve(currentUser(request), id(request));
        Flash.success(request, "Кредит одобрен, сумма зачислена на счёт клиента");
        return View.redirect("/loans");
    }

    private View reject(HttpServletRequest request, HttpServletResponse response) throws BankException {
        loans.reject(currentUser(request), id(request));
        Flash.success(request, "Заявка отклонена");
        return View.redirect("/loans");
    }

    private View pay(HttpServletRequest request, HttpServletResponse response) throws BankException {
        loans.pay(currentUser(request), id(request));
        Flash.success(request, "Платёж по кредиту проведён");
        return View.redirect("/loans");
    }

    private View delete(HttpServletRequest request, HttpServletResponse response) throws BankException {
        loans.delete(id(request));
        Flash.success(request, "Заявка удалена");
        return View.redirect("/loans");
    }
}
