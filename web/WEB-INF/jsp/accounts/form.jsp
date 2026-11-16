<c:set var="pageTitle" value="Открытие счёта"/>
<c:set var="chosenClient" value="${empty param.clientId ? selectedClientId : param.clientId}"/>
<%@ include file="/WEB-INF/jspf/header.jspf" %>
<form class="card form" method="post" action="${app}/accounts/open" data-validate novalidate>
    <b:csrf/>
    <b:hasRole role="ADMIN">
        <t:select name="clientId" label="Клиент" required="true">
            <option value="">Выберите клиента</option>
            <c:forEach var="client" items="${clients}">
                <option value="${client.id}" ${client.id == chosenClient ? 'selected' : ''}><c:out value="${client.fullName}"/></option>
            </c:forEach>
        </t:select>
    </b:hasRole>
    <t:select name="typeId" label="Вид счёта" required="true">
        <option value="">Выберите вид счёта</option>
        <c:forEach var="type" items="${types}">
            <option value="${type.id}" ${type.id == param.typeId ? 'selected' : ''}><c:out value="${type.name}"/> (${type.annualRate}% годовых)</option>
        </c:forEach>
    </t:select>
    <t:select name="currencyId" label="Валюта" required="true">
        <option value="">Выберите валюту</option>
        <c:forEach var="currency" items="${currencies}">
            <option value="${currency.id}" ${currency.id == param.currencyId ? 'selected' : ''}><c:out value="${currency.title}"/></option>
        </c:forEach>
    </t:select>
    <div class="form-actions">
        <button type="submit" class="btn btn-primary">Открыть</button>
        <a class="btn" href="${app}/accounts">Отмена</a>
    </div>
</form>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
