<c:set var="pageTitle" value="Главная"/>
<%@ include file="/WEB-INF/jspf/header.jspf" %>
<%@ include file="/WEB-INF/jspf/stats.jspf" %>
<c:if test="${currentUser.hasRole('USER')}">
    <section class="card">
        <div class="card-head">
            <h2>Мои счета</h2>
            <a class="btn" href="${app}/accounts/new">Открыть счёт</a>
        </div>
        <%@ include file="/WEB-INF/jspf/accounts-table.jspf" %>
    </section>
</c:if>
<section class="card">
    <div class="card-head">
        <h2>Последние операции</h2>
        <a class="btn" href="${app}/operations">Все операции</a>
    </div>
    <%@ include file="/WEB-INF/jspf/operations-table.jspf" %>
</section>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
