package com.bank.controller;

import com.bank.exception.NotFoundException;
import com.bank.exception.ValidationException;
import com.bank.model.User;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.sql.Date;

/** Группа команд одного раздела. Содержит общие методы разбора параметров запроса. */
public abstract class CommandModule {
    /** Регистрирует маршруты раздела. */
    public abstract void register(CommandRegistry registry);

    protected static User currentUser(HttpServletRequest request) {
        return (User) request.getAttribute("currentUser");
    }

    /** Значение параметра без пробелов по краям; пустая строка заменяется на null. */
    protected static String text(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        if (value == null) {
            return null;
        }
        value = value.trim();
        return value.isEmpty() ? null : value;
    }

    protected static Integer integer(HttpServletRequest request, String name) {
        String value = text(request, name);
        if (value == null) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    protected static int id(HttpServletRequest request) throws NotFoundException {
        Integer id = integer(request, "id");
        if (id == null) {
            throw new NotFoundException("Не указан идентификатор записи");
        }
        return id;
    }

    protected static BigDecimal decimal(HttpServletRequest request, String name) {
        String value = text(request, name);
        if (value == null) {
            return null;
        }
        try {
            return new BigDecimal(value.replace(',', '.').replace(" ", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    protected static Date date(HttpServletRequest request, String name) {
        String value = text(request, name);
        if (value == null) {
            return null;
        }
        try {
            return Date.valueOf(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    protected static void showErrors(HttpServletRequest request, ValidationException e) {
        request.setAttribute("errors", e.getErrors());
    }
}
