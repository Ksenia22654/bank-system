<c:set var="pageTitle" value="Заявка на кредит"/>
<%@ include file="/WEB-INF/jspf/header.jspf" %>
<form class="card form" method="post" action="${app}/loans/apply" data-validate novalidate>
    <b:csrf/>
    <t:select name="accountId" label="Счёт для зачисления и платежей" required="true">
        <option value="">Выберите счёт</option>
        <c:forEach var="a" items="${accounts}">
            <c:if test="${a.active}">
                <option value="${a.id}" ${a.id == param.accountId ? 'selected' : ''}><c:out value="${a.accountNo}"/><c:if test="${currentUser.admin}"> — <c:out value="${a.clientName}"/></c:if> (<c:out value="${a.currencyCode}"/>)</option>
            </c:if>
        </c:forEach>
    </t:select>
    <t:field name="amount" label="Сумма кредита" type="number" value="${param.amount}" required="true" min="0.01" step="0.01"/>
    <t:field name="termMonths" label="Срок, месяцев" type="number" value="${param.termMonths}" required="true" min="1" max="360" step="1"/>
    <p class="muted">Базовая ставка — ${defaultRate}% годовых. Окончательную ставку устанавливает банк при рассмотрении заявки.</p>
    <div class="form-actions">
        <button type="submit" class="btn btn-primary">Отправить заявку</button>
        <a class="btn" href="${app}/loans">Отмена</a>
    </div>
</form>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
