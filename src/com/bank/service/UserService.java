package com.bank.service;

import com.bank.dao.Database;
import com.bank.dao.UserDao;
import com.bank.exception.BankException;
import com.bank.exception.BusinessRuleException;
import com.bank.exception.NotFoundException;
import com.bank.model.User;
import com.bank.util.PasswordHasher;
import com.bank.util.Validator;

import java.util.List;

public class UserService {
    private static final String PASSWORD_REGEX = ".{6,100}";

    private final UserDao users = new UserDao();

    /** Возвращает пользователя при верных логине и пароле, иначе null. */
    public User authenticate(String login, String password) {
        if (login == null || password == null) {
            return null;
        }
        User user = users.findByLogin(login.trim());
        if (user == null || !user.isActive() || !PasswordHasher.matches(password, user.getPasswordHash())) {
            return null;
        }
        return user;
    }

    public User find(int id) {
        return users.findById(id);
    }

    public List<User> list() {
        return users.findAll();
    }

    public User get(int id) throws NotFoundException {
        User user = users.findById(id);
        if (user == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        return user;
    }

    /** Сохраняет учётную запись. Пароль при редактировании необязателен: пустой оставляет прежний. */
    public void save(User u, String password) throws BankException {
        Validator v = new Validator();
        v.required("login", u.getLogin(), "Логин")
                .pattern("login", u.getLogin(), "[A-Za-z0-9_.-]{3,30}", "Логин: от 3 до 30 латинских букв, цифр и знаков _ . -");
        if (u.isNew()) {
            v.required("password", password, "Пароль");
        }
        if (password != null && !password.isEmpty()) {
            v.pattern("password", password, PASSWORD_REGEX, "Пароль не короче 6 символов");
        }
        if (!User.ADMIN.equals(u.getRoleCode()) && !User.USER.equals(u.getRoleCode())) {
            v.add("roleCode", "Выберите роль");
        }
        if (User.USER.equals(u.getRoleCode()) && u.getClientId() == null) {
            v.add("clientId", "Пользователь должен быть связан с клиентом");
        }
        User sameLogin = u.getLogin() == null ? null : users.findByLogin(u.getLogin());
        if (sameLogin != null && sameLogin.getId() != u.getId()) {
            v.add("login", "Такой логин уже занят");
        }
        v.throwIfInvalid();
        if (User.ADMIN.equals(u.getRoleCode())) {
            u.setClientId(null);
        }
        Database.getInstance().inTransaction(() -> {
            if (!u.isNew()) {
                User old = get(u.getId());
                if (old.isAdmin() && old.isActive() && !(u.isAdmin() && u.isActive())) {
                    requireAnotherAdmin();
                }
                u.setPasswordHash(old.getPasswordHash());
            }
            if (password != null && !password.isEmpty()) {
                u.setPasswordHash(PasswordHasher.hash(password));
            }
            users.save(u);
            return null;
        });
    }

    public void delete(User actor, int id) throws BankException {
        if (actor.getId() == id) {
            throw new BusinessRuleException("Нельзя удалить собственную учётную запись");
        }
        User user = get(id);
        if (user.isAdmin() && user.isActive()) {
            requireAnotherAdmin();
        }
        users.delete(id);
    }

    public void changePassword(User user, String current, String next, String repeat) throws BankException {
        User stored = get(user.getId());
        Validator v = new Validator();
        v.required("current", current, "Текущий пароль");
        v.required("next", next, "Новый пароль").pattern("next", next, PASSWORD_REGEX, "Пароль не короче 6 символов");
        if (next != null && !next.equals(repeat)) {
            v.add("repeat", "Пароли не совпадают");
        }
        if (current != null && !current.isEmpty() && !PasswordHasher.matches(current, stored.getPasswordHash())) {
            v.add("current", "Текущий пароль указан неверно");
        }
        v.throwIfInvalid();
        users.updatePassword(user.getId(), PasswordHasher.hash(next));
    }

    private void requireAnotherAdmin() throws BusinessRuleException {
        if (users.findActiveAdmins().size() <= 1) {
            throw new BusinessRuleException("В системе должен остаться хотя бы один активный администратор");
        }
    }
}
