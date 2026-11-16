package com.bank.controller;

import com.bank.exception.AccessDeniedException;
import com.bank.exception.BankException;
import com.bank.exception.DataAccessException;
import com.bank.exception.NotFoundException;
import com.bank.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Единственный контроллер MVC. Контейнер Tomcat обслуживает запросы в пуле потоков,
 * поэтому сервлет не хранит изменяемого состояния запроса в полях.
 */
@WebServlet(urlPatterns = "/app/*", loadOnStartup = 1)
public class FrontControllerServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(FrontControllerServlet.class.getName());

    private transient CommandRegistry registry;

    @Override
    public void init() {
        registry = new CommandRegistry();
        List<CommandModule> modules = Arrays.asList(
                new AuthModule(), new HomeModule(), new ClientModule(), new AccountModule(), new OperationModule(),
                new LoanModule(), new ReferenceModule(), new UserModule(), new AnalyticsModule(),
                new ReportModule(), new BackupModule());
        modules.forEach(module -> module.register(registry));
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getPathInfo() == null ? "/" : request.getPathInfo();
        CommandRegistry.Route route = registry.find(request.getMethod(), path);
        if (route == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        User user = (User) request.getAttribute("currentUser");
        if (route.getRole() != null && (user == null || !user.hasRole(route.getRole()))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        try {
            route.getCommand().execute(request, response).render(request, response);
        } catch (NotFoundException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (AccessDeniedException e) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        } catch (BankException e) {
            backWithError(request, response, e.getMessage());
        } catch (DataAccessException e) {
            if (e.isConstraintViolation()) {
                backWithError(request, response, describe(e));
            } else {
                LOG.log(Level.SEVERE, "Ошибка базы данных", e);
                throw new ServletException(e);
            }
        }
    }

    private static String describe(DataAccessException e) {
        return "23505".equals(e.getSqlState())
                ? "Запись с такими данными уже существует"
                : "Запись связана с другими данными, поэтому действие невозможно";
    }

    private static void backWithError(HttpServletRequest request, HttpServletResponse response, String message)
            throws IOException {
        Flash.error(request, message);
        response.sendRedirect(backTarget(request));
    }

    /** Возврат на страницу, с которой пришёл запрос; адреса других сайтов не используются. */
    private static String backTarget(HttpServletRequest request) {
        String home = request.getContextPath() + "/app/home";
        String referer = request.getHeader("Referer");
        if (referer == null) {
            return home;
        }
        try {
            URI uri = new URI(referer);
            String target = uri.getRawPath();
            if (target != null && target.startsWith(request.getContextPath() + "/app/")
                    && request.getServerName().equalsIgnoreCase(uri.getHost())) {
                return uri.getRawQuery() == null ? target : target + "?" + uri.getRawQuery();
            }
        } catch (URISyntaxException e) {
            return home;
        }
        return home;
    }
}
