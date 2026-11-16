<c:set var="pageTitle" value="Виды счетов"/>
<%@ include file="/WEB-INF/jspf/header.jspf" %>
<div class="split">
    <section class="card">
        <div class="table-wrap">
            <table>
                <thead><tr><th>Название</th><th class="num">Ставка, % годовых</th><th></th></tr></thead>
                <tbody>
                <c:forEach var="type" items="${items}">
                    <tr>
                        <td><c:out value="${type.name}"/></td>
                        <td class="num">${type.annualRate}</td>
                        <td class="row-actions">
                            <a class="btn btn-small" href="${app}/account-types?id=${type.id}">Изменить</a>
                            <form method="post" action="${app}/account-types/delete" data-confirm="Удалить вид счёта?">
                                <b:csrf/><input type="hidden" name="id" value="${type.id}">
                                <button type="submit" class="btn btn-small btn-danger">Удалить</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </section>
    <form class="card form" method="post" action="${app}/account-types/save" data-validate novalidate>
        <h2>${item.persisted ? 'Изменение вида счёта' : 'Новый вид счёта'}</h2>
        <b:csrf/>
        <input type="hidden" name="id" value="${item.id}">
        <t:field name="name" label="Название" value="${item.name}" required="true" maxlength="50"/>
        <t:field name="annualRate" label="Ставка, % годовых" type="number" value="${item.annualRate}" required="true" min="0" max="100" step="0.01"/>
        <div class="form-actions">
            <button type="submit" class="btn btn-primary">Сохранить</button>
            <c:if test="${item.persisted}"><a class="btn" href="${app}/account-types">Отмена</a></c:if>
        </div>
    </form>
</div>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
