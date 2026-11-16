<c:set var="pageTitle" value="${currentUser.admin ? 'Счета' : 'Мои счета'}"/>
<%@ include file="/WEB-INF/jspf/header.jspf" %>
<div class="toolbar">
    <b:hasRole role="ADMIN">
        <form class="search" method="get" action="${app}/accounts" role="search">
            <label class="sr-only" for="q">Поиск</label>
            <input id="q" type="search" name="q" value="<c:out value='${q}'/>" placeholder="Номер счёта или клиент">
            <button type="submit" class="btn">Найти</button>
        </form>
        <a class="btn" href="${app}/operations/deposit">Пополнение</a>
        <a class="btn" href="${app}/operations/withdraw">Снятие</a>
    </b:hasRole>
    <a class="btn btn-primary" href="${app}/accounts/new">Открыть счёт</a>
</div>
<section class="card">
    <%@ include file="/WEB-INF/jspf/accounts-table.jspf" %>
</section>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
