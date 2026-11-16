<c:set var="pageTitle" value="${client.fullName}"/>
<%@ include file="/WEB-INF/jspf/header.jspf" %>
<div class="toolbar">
    <a class="btn" href="${app}/clients">К списку</a>
    <a class="btn" href="${app}/clients/edit?id=${client.id}">Изменить</a>
    <a class="btn btn-primary" href="${app}/accounts/new?clientId=${client.id}">Открыть счёт</a>
</div>
<section class="card">
    <h2>Данные клиента</h2>
    <dl class="details">
        <dt>Дата рождения</dt><dd><fmt:formatDate value="${client.birthDate}" pattern="dd.MM.yyyy"/></dd>
        <dt>Паспорт</dt><dd><c:out value="${client.passportNo}"/></dd>
        <dt>Телефон</dt><dd><c:out value="${client.phone}"/></dd>
        <dt>Эл. почта</dt><dd><c:out value="${client.email}" default="—"/></dd>
        <dt>Адрес</dt><dd><c:out value="${client.address}"/></dd>
        <dt>Клиент с</dt><dd><fmt:formatDate value="${client.registeredOn}" pattern="dd.MM.yyyy"/></dd>
    </dl>
</section>
<section class="card">
    <h2>Счета</h2>
    <%@ include file="/WEB-INF/jspf/accounts-table.jspf" %>
</section>
<section class="card">
    <h2>Кредиты</h2>
    <c:choose>
        <c:when test="${empty loans}"><p class="empty">Кредитов нет.</p></c:when>
        <c:otherwise>
            <div class="table-wrap">
                <table>
                    <thead><tr><th>№</th><th class="num">Сумма</th><th class="num">Ставка</th><th class="num">Срок</th><th>Статус</th></tr></thead>
                    <tbody>
                    <c:forEach var="loan" items="${loans}">
                        <tr>
                            <td>${loan.id}</td>
                            <td class="num nowrap"><b:money value="${loan.amount}" currency="${loan.currencyCode}"/></td>
                            <td class="num">${loan.annualRate}%</td>
                            <td class="num">${loan.termMonths} мес.</td>
                            <td><c:out value="${loan.statusTitle}"/></td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>
</section>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
