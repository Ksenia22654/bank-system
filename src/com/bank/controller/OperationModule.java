package com.bank.controller;

import com.bank.exception.BankException;
import com.bank.exception.BusinessRuleException;
import com.bank.exception.ValidationException;
import com.bank.model.User;
import com.bank.service.AccountService;
import com.bank.service.OperationService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;

/** Операции: журнал, пополнение, снятие, перевод, начисление процентов. */
public class OperationModule extends CommandModule {
    private final OperationService operations = new OperationService();
    private final AccountService accounts = new AccountService();

    @Override
    public void register(CommandRegistry r) {
        r.get("/operations", this::list);
        r.get("/operations/deposit", User.ADMIN, (req, resp) -> cashForm(req, "deposit"));
        r.get("/operations/withdraw", User.ADMIN, (req, resp) -> cashForm(req, "withdraw"));
        r.post("/operations/deposit", User.ADMIN, this::deposit);
        r.post("/operations/withdraw", User.ADMIN, this::withdraw);
        r.get("/operations/transfer", (req, resp) -> transferForm(req));
        r.post("/operations/transfer", this::transfer);
        r.post("/operations/interest", User.ADMIN, this::accrue);
    }

    private View list(HttpServletRequest request, HttpServletResponse response) {
        request.setAttribute("operations",
                operations.listFor(currentUser(request), date(request, "from"), date(request, "to")));
        return View.jsp("operations/list");
    }

    private View cashForm(HttpServletRequest request, String kind) {
        request.setAttribute("kind", kind);
        request.setAttribute("accounts", accounts.listFor(currentUser(request), null));
        return View.jsp("operations/cash");
    }

    private View transferForm(HttpServletRequest request) {
        request.setAttribute("accounts", accounts.listFor(currentUser(request), null));
        return View.jsp("operations/transfer");
    }

    private View deposit(HttpServletRequest request, HttpServletResponse response) throws BankException {
        Integer accountId = integer(request, "accountId");
        BigDecimal amount = decimal(request, "amount");
        try {
            operations.deposit(currentUser(request), accountId, amount, text(request, "description"));
        } catch (ValidationException | BusinessRuleException e) {
            return reshow(request, e, () -> cashForm(request, "deposit"));
        }
        Flash.success(request, "Счёт пополнен");
        return View.redirect("/accounts/view?id=" + accountId);
    }

    private View withdraw(HttpServletRequest request, HttpServletResponse response) throws BankException {
        Integer accountId = integer(request, "accountId");
        BigDecimal amount = decimal(request, "amount");
        try {
            operations.withdraw(currentUser(request), accountId, amount, text(request, "description"));
        } catch (ValidationException | BusinessRuleException e) {
            return reshow(request, e, () -> cashForm(request, "withdraw"));
        }
        Flash.success(request, "Снятие проведено");
        return View.redirect("/accounts/view?id=" + accountId);
    }

    private View transfer(HttpServletRequest request, HttpServletResponse response) throws BankException {
        Integer fromId = integer(request, "fromId");
        try {
            operations.transfer(currentUser(request), fromId, text(request, "toNumber"),
                    decimal(request, "amount"), text(request, "description"));
        } catch (ValidationException | BusinessRuleException e) {
            return reshow(request, e, () -> transferForm(request));
        }
        Flash.success(request, "Перевод выполнен");
        return View.redirect("/accounts/view?id=" + fromId);
    }

    private View accrue(HttpServletRequest request, HttpServletResponse response) throws BankException {
        int count = operations.accrueInterest(currentUser(request));
        Flash.success(request, count == 0
                ? "Новых начислений нет: проценты за этот месяц уже начислены"
                : "Проценты начислены, счетов: " + count);
        return View.redirect("/operations");
    }

    /** Показывает форму снова с введёнными значениями и текстом ошибки. */
    private View reshow(HttpServletRequest request, BankException e, java.util.function.Supplier<View> form) {
        if (e instanceof ValidationException) {
            showErrors(request, (ValidationException) e);
        } else {
            request.setAttribute("formError", e.getMessage());
        }
        return form.get();
    }
}
