package com.bank.tag;

import com.bank.util.Money;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;
import java.math.BigDecimal;

/** Выводит денежную сумму: {@code <b:money value="${a.balance}" currency="${a.currencyCode}"/>}. */
public class MoneyTag extends SimpleTagSupport {
    private BigDecimal value;
    private String currency;

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @Override
    public void doTag() throws JspException, IOException {
        boolean withCurrency = currency != null && !currency.isEmpty();
        getJspContext().getOut().write(withCurrency ? Money.format(value, currency) : Money.format(value));
    }
}
