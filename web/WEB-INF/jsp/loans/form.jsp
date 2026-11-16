<c:set var="pageTitle" value="Заявка на кредит №${loan.id}"/>
<%@ include file="/WEB-INF/jspf/header.jspf" %>
<form class="card form" method="post" action="${app}/loans/save" data-validate novalidate>
    <b:csrf/>
    <input type="hidden" name="id" value="${loan.id}">
    <p class="muted"><c:out value="${loan.clientName}"/>, счёт <c:out value="${loan.accountNo}"/> (<c:out value="${loan.currencyCode}"/>)</p>
    <t:field name="amount" label="Сумма кредита" type="number" value="${empty param.amount ? loan.amount : param.amount}" required="true" min="0.01" step="0.01"/>
    <t:field name="annualRate" label="Ставка, % годовых" type="number" value="${empty param.annualRate ? loan.annualRate : param.annualRate}" required="true" min="0" max="100" step="0.01"/>
    <t:field name="termMonths" label="Срок, месяцев" type="number" value="${empty param.termMonths ? loan.termMonths : param.termMonths}" required="true" min="1" max="360" step="1"/>
    <div class="form-actions">
        <button type="submit" class="btn btn-primary">Сохранить</button>
        <a class="btn" href="${app}/loans">Отмена</a>
    </div>
</form>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
