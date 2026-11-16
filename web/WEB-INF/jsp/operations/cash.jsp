<c:set var="deposit" value="${kind == 'deposit'}"/>
<c:set var="pageTitle" value="${deposit ? 'Пополнение счёта' : 'Снятие со счёта'}"/>
<%@ include file="/WEB-INF/jspf/header.jspf" %>
<form class="card form" method="post" action="${app}/operations/${kind}" data-validate novalidate>
    <b:csrf/>
    <c:if test="${not empty formError}"><div class="alert alert-error" role="alert"><c:out value="${formError}"/></div></c:if>
    <t:select name="accountId" label="Счёт" required="true">
        <option value="">Выберите счёт</option>
        <c:forEach var="a" items="${accounts}">
            <c:if test="${a.active}">
                <option value="${a.id}" ${a.id == param.accountId ? 'selected' : ''}><c:out value="${a.accountNo} — ${a.clientName} (${a.currencyCode})"/></option>
            </c:if>
        </c:forEach>
    </t:select>
    <t:field name="amount" label="Сумма" type="number" value="${param.amount}" required="true" min="0.01" step="0.01"/>
    <t:field name="description" label="Назначение" value="${param.description}" maxlength="200"/>
    <div class="form-actions">
        <button type="submit" class="btn btn-primary">${deposit ? 'Пополнить' : 'Снять'}</button>
        <a class="btn" href="${app}/operations">Отмена</a>
    </div>
</form>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
