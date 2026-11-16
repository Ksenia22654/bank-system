package com.bank.tag;

import com.bank.web.CsrfToken;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;

/** Добавляет в форму скрытое поле с токеном CSRF. */
public class CsrfTag extends SimpleTagSupport {
    @Override
    public void doTag() throws JspException, IOException {
        PageContext page = (PageContext) getJspContext();
        String token = CsrfToken.forSession(page.getSession());
        page.getOut().write("<input type=\"hidden\" name=\"" + CsrfToken.PARAMETER + "\" value=\"" + token + "\">");
    }
}
