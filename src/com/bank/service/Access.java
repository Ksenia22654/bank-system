package com.bank.service;

import com.bank.exception.AccessDeniedException;
import com.bank.model.User;

/** Проверка прав: администратору доступно всё, пользователю — только его собственные данные. */
public final class Access {
    private Access() {
    }

    public static void requireOwner(User user, int clientId) throws AccessDeniedException {
        boolean owner = user.getClientId() != null && user.getClientId() == clientId;
        if (!user.isAdmin() && !owner) {
            throw new AccessDeniedException("Эти данные принадлежат другому клиенту");
        }
    }

    public static void requireAdmin(User user) throws AccessDeniedException {
        if (!user.isAdmin()) {
            throw new AccessDeniedException("Действие доступно только администратору");
        }
    }
}
