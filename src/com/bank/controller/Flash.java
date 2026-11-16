package com.bank.controller;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

/** Сообщение для пользователя, которое показывается один раз на следующей странице. */
public class Flash implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String kind;
    private final String text;

    private Flash(String kind, String text) {
        this.kind = kind;
        this.text = text;
    }

    public static void success(HttpServletRequest request, String text) {
        request.getSession().setAttribute("flash", new Flash("success", text));
    }

    public static void error(HttpServletRequest request, String text) {
        request.getSession().setAttribute("flash", new Flash("error", text));
    }

    public String getKind() {
        return kind;
    }

    public String getText() {
        return text;
    }
}
