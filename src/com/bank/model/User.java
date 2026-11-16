package com.bank.model;

/** Учётная запись. Пользователь с ролью USER связан с клиентом банка. */
public class User extends BaseEntity {
    private static final long serialVersionUID = 1L;

    public static final String ADMIN = "ADMIN";
    public static final String USER = "USER";

    private String login;
    private String passwordHash;
    private String roleCode;
    private Integer clientId;
    private boolean active = true;

    private String clientName;

    public boolean hasRole(String role) {
        return role != null && role.equals(roleCode);
    }

    public boolean isAdmin() {
        return hasRole(ADMIN);
    }

    public String getRoleTitle() {
        return isAdmin() ? "Администратор" : "Пользователь";
    }

    @Override
    public String getTitle() {
        return login;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public Integer getClientId() {
        return clientId;
    }

    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }
}
