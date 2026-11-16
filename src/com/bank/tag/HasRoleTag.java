package com.bank.tag;

import com.bank.model.User;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;

/** Показывает содержимое тега только пользователю с указанной ролью. */
public class HasRoleTag extends SimpleTagSupport {
    private String role;

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public void doTag() throws JspException, IOException {
        User user = (User) ((PageContext) getJspContext()).getRequest().getAttribute("currentUser");
        if (user != null && user.hasRole(role) && getJspBody() != null) {
            getJspBody().invoke(null);
        }
    }
}
