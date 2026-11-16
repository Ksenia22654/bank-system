package com.bank.controller;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Результат команды: страница JSP, перенаправление или уже сформированный ответ. */
public abstract class View {
    public abstract void render(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException;

    public static View jsp(String name) {
        return new JspView(name);
    }

    public static View redirect(String path) {
        return new RedirectView(path);
    }

    public static View none() {
        return new CommittedView();
    }

    private static final class JspView extends View {
        private final String name;

        JspView(String name) {
            this.name = name;
        }

        @Override
        public void render(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException {
            request.getRequestDispatcher("/WEB-INF/jsp/" + name + ".jsp").forward(request, response);
        }
    }

    private static final class RedirectView extends View {
        private final String path;

        RedirectView(String path) {
            this.path = path;
        }

        @Override
        public void render(HttpServletRequest request, HttpServletResponse response) throws IOException {
            response.sendRedirect(request.getContextPath() + "/app" + path);
        }
    }

    private static final class CommittedView extends View {
        @Override
        public void render(HttpServletRequest request, HttpServletResponse response) {
            // Команда уже записала ответ сама.
        }
    }
}
