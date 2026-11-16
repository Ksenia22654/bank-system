<c:set var="pageTitle" value="Аналитика"/>
<%@ include file="/WEB-INF/jspf/header.jspf" %>
<%@ include file="/WEB-INF/jspf/stats.jspf" %>
<div class="charts" data-charts="${app}/analytics/data">
    <section class="card chart-card chart-wide">
        <h2>Зачисления и списания по месяцам</h2>
        <p class="muted">За последние 6 месяцев, в рублях по текущему курсу</p>
        <div class="chart" id="chart-turnover"></div>
        <details class="chart-table"><summary>Показать таблицей</summary><div id="table-turnover"></div></details>
    </section>
    <section class="card chart-card">
        <h2 id="balances-title">Остатки</h2>
        <p class="muted">В рублях по текущему курсу</p>
        <div class="chart" id="chart-balances"></div>
        <details class="chart-table"><summary>Показать таблицей</summary><div id="table-balances"></div></details>
    </section>
    <b:hasRole role="ADMIN">
        <section class="card chart-card">
            <h2>Открытые счета по видам</h2>
            <p class="muted">Количество счетов</p>
            <div class="chart" id="chart-types"></div>
            <details class="chart-table"><summary>Показать таблицей</summary><div id="table-types"></div></details>
        </section>
    </b:hasRole>
</div>
<script src="${ctx}/static/js/charts.js" defer></script>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
