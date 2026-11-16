<c:set var="pageTitle" value="Валюты"/>
<%@ include file="/WEB-INF/jspf/header.jspf" %>
<div class="split">
    <section class="card">
        <div class="table-wrap">
            <table>
                <thead><tr><th>Код</th><th>Название</th><th class="num">Курс, RUB</th><th></th></tr></thead>
                <tbody>
                <c:forEach var="c" items="${items}">
                    <tr>
                        <td><c:out value="${c.code}"/></td>
                        <td><c:out value="${c.name}"/></td>
                        <td class="num">${c.rate}</td>
                        <td class="row-actions">
                            <a class="btn btn-small" href="${app}/currencies?id=${c.id}">Изменить</a>
                            <form method="post" action="${app}/currencies/delete" data-confirm="Удалить валюту?">
                                <b:csrf/><input type="hidden" name="id" value="${c.id}">
                                <button type="submit" class="btn btn-small btn-danger">Удалить</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </section>
    <form class="card form" method="post" action="${app}/currencies/save" data-validate novalidate>
        <h2>${item.persisted ? 'Изменение валюты' : 'Новая валюта'}</h2>
        <b:csrf/>
        <input type="hidden" name="id" value="${item.id}">
        <t:field name="code" label="Код" value="${item.code}" required="true" pattern="[A-Z]{3}" hint="Три латинские заглавные буквы" maxlength="3"/>
        <t:field name="name" label="Название" value="${item.name}" required="true" maxlength="50"/>
        <t:field name="rate" label="Курс к рублю" type="number" value="${item.rate}" required="true" min="0.0001" step="0.0001"/>
        <div class="form-actions">
            <button type="submit" class="btn btn-primary">Сохранить</button>
            <c:if test="${item.persisted}"><a class="btn" href="${app}/currencies">Отмена</a></c:if>
        </div>
    </form>
</div>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
