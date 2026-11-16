package com.bank.web;

import com.bank.model.User;
import com.bank.service.UserService;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Пропускает к контроллеру только вошедших пользователей. Учётная запись читается из базы при каждом
 * запросе, поэтому блокировка и смена роли действуют сразу. Также защищает POST-запросы токеном CSRF.
 */
@WebFilter(filterName = "AuthFilter", urlPatterns = "/app/*")
public class AuthFilter implements Filter {
    private final UserService users = new UserService();

    @Override
    public void init(FilterConfig config) {
        // Настройка не требуется.
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;
        response.setHeader("Cache-Control", "no-store");
        String path = request.getPathInfo();
        HttpSession session = request.getSession(true);
        if ("POST".equals(request.getMethod()) && !CsrfToken.isValid(request)) {
            // Сессия без токена означает, что прежняя истекла: пользователь входит заново.
            if (session.getAttribute(CsrfToken.PARAMETER) == null) {
                response.sendRedirect(request.getContextPath() + "/app/login");
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
            return;
        }
        if ("/login".equals(path)) {
            chain.doFilter(request, response);
            return;
        }
        Integer userId = (Integer) session.getAttribute("userId");
        User user = userId == null ? null : users.find(userId);
        if (user == null || !user.isActive()) {
            session.invalidate();
            response.sendRedirect(request.getContextPath() + "/app/login");
            return;
        }
        request.setAttribute("currentUser", user);
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Ресурсов для освобождения нет.
    }
}
