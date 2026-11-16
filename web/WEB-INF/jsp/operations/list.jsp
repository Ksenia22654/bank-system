<c:set var="pageTitle" value="Операции"/>
<%@ include file="/WEB-INF/jspf/header.jspf" %>
<div class="toolbar">
    <form class="search" method="get" action="${app}/operations">
        <label for="from">С</label>
        <input id="from" type="date" name="from" value="<c:out value='${param.from}'/>">
        <label for="to">по</label>
        <input id="to" type="date" name="to" value="<c:out value='${param.to}'/>">
        <button type="submit" class="btn">Показать</button>
    </form>
    <a class="btn" href="${app}/reports/download?type=operations&amp;from=<c:out value='${param.from}'/>&amp;to=<c:out value='${param.to}'/>">Сохранить в файл</a>
    <a class="btn btn-primary" href="${app}/operations/transfer">Перевод</a>
    <b:hasRole role="ADMIN">
        <a class="btn" href="${app}/operations/deposit">Пополнение</a>
        <a class="btn" href="${app}/operations/withdraw">Снятие</a>
        <form method="post" action="${app}/operations/interest" data-confirm="Начислить проценты за текущий месяц на все счета с процентной ставкой?">
            <b:csrf/>
            <button type="submit" class="btn">Начислить проценты</button>
        </form>
    </b:hasRole>
</div>
<section class="card">
    <%@ include file="/WEB-INF/jspf/operations-table.jspf" %>
</section>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
