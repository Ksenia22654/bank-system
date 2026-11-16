package com.bank.controller;

import com.bank.exception.AccessDeniedException;
import com.bank.exception.BankException;
import com.bank.exception.NotFoundException;
import com.bank.model.Account;
import com.bank.model.User;
import com.bank.report.AccountStatementReport;
import com.bank.report.AccountsReport;
import com.bank.report.ClientsReport;
import com.bank.report.LoansReport;
import com.bank.report.OperationsReport;
import com.bank.report.TextReport;
import com.bank.service.AccountService;
import com.bank.service.ClientService;
import com.bank.service.LoanService;
import com.bank.service.OperationService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/** Формирование текстовых отчётов и их сохранение в файл на компьютере пользователя. */
public class ReportModule extends CommandModule {
    private static final byte[] UTF8_BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    private final ClientService clients = new ClientService();
    private final AccountService accounts = new AccountService();
    private final OperationService operations = new OperationService();
    private final LoanService loans = new LoanService();

    @Override
    public void register(CommandRegistry r) {
        r.get("/reports", (req, resp) -> View.jsp("reports"));
        r.get("/reports/download", this::download);
    }

    private View download(HttpServletRequest request, HttpServletResponse response)
            throws BankException, IOException {
        TextReport report = build(request);
        byte[] body = report.render().getBytes(StandardCharsets.UTF_8);
        response.setContentType("text/plain;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + report.getFileName() + "\"");
        response.setContentLength(body.length + UTF8_BOM.length);
        response.getOutputStream().write(UTF8_BOM);
        response.getOutputStream().write(body);
        return View.none();
    }

    private TextReport build(HttpServletRequest request) throws BankException {
        User user = currentUser(request);
        String type = request.getParameter("type");
        if (type == null) {
            throw new NotFoundException("Не указан вид отчёта");
        }
        switch (type) {
            case "clients":
                if (!user.isAdmin()) {
                    throw new AccessDeniedException("Отчёт доступен только администратору");
                }
                return new ClientsReport(clients.search(null));
            case "accounts":
                return new AccountsReport(accounts.listFor(user, null));
            case "operations":
                return new OperationsReport(operations.listFor(user, date(request, "from"), date(request, "to")));
            case "loans":
                return new LoansReport(loans.listFor(user));
            case "statement":
                Account account = accounts.get(user, id(request));
                return new AccountStatementReport(account, operations.forAccount(account.getId()));
            default:
                throw new NotFoundException("Неизвестный вид отчёта");
        }
    }
}
