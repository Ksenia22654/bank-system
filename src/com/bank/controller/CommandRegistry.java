package com.bank.controller;

import java.util.HashMap;
import java.util.Map;

/** Таблица маршрутов приложения: метод и путь сопоставляются с командой и требуемой ролью. */
public class CommandRegistry {
    public static final class Route {
        private final Command command;
        private final String role;

        Route(Command command, String role) {
            this.command = command;
            this.role = role;
        }

        public Command getCommand() {
            return command;
        }

        public String getRole() {
            return role;
        }
    }

    private final Map<String, Route> routes = new HashMap<>();

    public void get(String path, Command command) {
        routes.put("GET " + path, new Route(command, null));
    }

    public void get(String path, String role, Command command) {
        routes.put("GET " + path, new Route(command, role));
    }

    public void post(String path, Command command) {
        routes.put("POST " + path, new Route(command, null));
    }

    public void post(String path, String role, Command command) {
        routes.put("POST " + path, new Route(command, role));
    }

    public Route find(String method, String path) {
        return routes.get(method + " " + path);
    }
}
