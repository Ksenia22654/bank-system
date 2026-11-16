<c:set var="pageTitle" value="Счёт ${account.accountNo}"/>
<%@ include file="/WEB-INF/jspf/header.jspf" %>
<div class="toolbar">
    <a class="btn" href="${app}/accounts">К списку</a>
    <a class="btn" href="${app}/reports/download?type=statement&amp;id=${account.id}">Сохранить выписку в файл</a>
    <c:if test="${account.active}">
        <a class="btn btn-primary" href="${app}/operations/transfer?fromId=${account.id}">Перевести</a>
        <b:hasRole role="ADMIN">
            <a class="btn" href="${app}/operations/deposit?accountId=${account.id}">Пополнить</a>
            <a class="btn" href="${app}/operations/withdraw?accountId=${account.id}">Снять</a>
        </b:hasRole>
        <form method="post" action="${app}/accounts/close" data-confirm="Закрыть этот счёт?">
            <b:csrf/><input type="hidden" name="id" value="${account.id}">
            <button type="submit" class="btn">Закрыть счёт</button>
        </form>
    </c:if>
    <b:hasRole role="ADMIN">
        <form method="post" action="${app}/accounts/delete" data-confirm="Удалить счёт? Счёт с операциями удалить нельзя.">
            <b:csrf/><input type="hidden" name="id" value="${account.id}">
            <button type="submit" class="btn btn-danger">Удалить</button>
        </form>
    </b:hasRole>
</div>
<section class="card">
    <dl class="details">
        <dt>Владелец</dt><dd><c:out value="${account.clientName}"/></dd>
        <dt>Вид счёта</dt><dd><c:out value="${account.typeName}"/> (${account.annualRate}% годовых)</dd>
        <dt>Остаток</dt><dd class="strong"><b:money value="${account.balance}" currency="${account.currencyCode}"/></dd>
        <dt>Открыт</dt><dd><fmt:formatDate value="${account.openedOn}" pattern="dd.MM.yyyy"/></dd>
        <dt>Состояние</dt>
        <dd>
            <c:choose>
                <c:when test="${account.active}"><span class="badge badge-on">Открыт</span></c:when>
                <c:otherwise><span class="badge badge-off">Закрыт <fmt:formatDate value="${account.closedOn}" pattern="dd.MM.yyyy"/></span></c:otherwise>
            </c:choose>
        </dd>
    </dl>
</section>
<section class="card">
    <h2>Выписка</h2>
    <%@ include file="/WEB-INF/jspf/operations-table.jspf" %>
</section>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
