<c:set var="pageTitle" value="Перевод средств"/>
<%@ include file="/WEB-INF/jspf/header.jspf" %>
<form class="card form" method="post" action="${app}/operations/transfer" data-validate novalidate>
    <b:csrf/>
    <c:if test="${not empty formError}"><div class="alert alert-error" role="alert"><c:out value="${formError}"/></div></c:if>
    <t:select name="fromId" label="Счёт списания" required="true">
        <option value="">Выберите счёт</option>
        <c:forEach var="a" items="${accounts}">
            <c:if test="${a.active}">
                <option value="${a.id}" ${a.id == param.fromId ? 'selected' : ''}>
                    <c:out value="${a.accountNo}"/><c:if test="${currentUser.admin}"> — <c:out value="${a.clientName}"/></c:if> (<b:money value="${a.balance}" currency="${a.currencyCode}"/>)
                </option>
            </c:if>
        </c:forEach>
    </t:select>
    <t:field name="toNumber" label="Номер счёта зачисления" value="${param.toNumber}" required="true"
             pattern="[0-9]{20}" hint="20 цифр" maxlength="20"/>
    <t:field name="amount" label="Сумма в валюте счёта списания" type="number" value="${param.amount}" required="true" min="0.01" step="0.01"/>
    <t:field name="description" label="Назначение" value="${param.description}" maxlength="200"/>
    <p class="muted">Если валюты счетов различаются, сумма пересчитывается по курсу банка.</p>
    <div class="form-actions">
        <button type="submit" class="btn btn-primary">Перевести</button>
        <a class="btn" href="${app}/operations">Отмена</a>
    </div>
</form>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
