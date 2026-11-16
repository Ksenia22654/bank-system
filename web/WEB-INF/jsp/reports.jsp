<c:set var="pageTitle" value="Отчёты"/>
<%@ include file="/WEB-INF/jspf/header.jspf" %>
<p class="muted">Отчёт формируется на сервере и сохраняется на ваш компьютер как текстовый файл.</p>
<div class="report-list">
    <b:hasRole role="ADMIN">
        <section class="card report">
            <h2>Клиенты</h2>
            <p>Список клиентов банка с паспортными и контактными данными.</p>
            <a class="btn btn-primary" href="${app}/reports/download?type=clients">Сохранить в файл</a>
        </section>
    </b:hasRole>
    <section class="card report">
        <h2>Счета</h2>
        <p>Счета, остатки и состояние.</p>
        <a class="btn btn-primary" href="${app}/reports/download?type=accounts">Сохранить в файл</a>
    </section>
    <section class="card report">
        <h2>Кредиты</h2>
        <p>Условия, ежемесячный платёж и остаток долга.</p>
        <a class="btn btn-primary" href="${app}/reports/download?type=loans">Сохранить в файл</a>
    </section>
    <section class="card report">
        <h2>Операции за период</h2>
        <form method="get" action="${app}/reports/download" class="report-form">
            <input type="hidden" name="type" value="operations">
            <label>С <input type="date" name="from"></label>
            <label>по <input type="date" name="to"></label>
            <button type="submit" class="btn btn-primary">Сохранить в файл</button>
        </form>
    </section>
</div>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
