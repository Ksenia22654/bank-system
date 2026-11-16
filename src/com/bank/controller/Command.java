package com.bank.controller;

import com.bank.exception.BankException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Команда обрабатывает один маршрут и возвращает представление (паттерн «Команда»). */
@FunctionalInterface
public interface Command {
    View execute(HttpServletRequest request, HttpServletResponse response)
            throws BankException, ServletException, IOException;
}
