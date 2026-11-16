<c:set var="pageTitle" value="Клиенты"/>
<%@ include file="/WEB-INF/jspf/header.jspf" %>
<div class="toolbar">
    <form class="search" method="get" action="${app}/clients" role="search">
        <label class="sr-only" for="q">Поиск</label>
        <input id="q" type="search" name="q" value="<c:out value='${q}'/>" placeholder="ФИО, паспорт или телефон">
        <button type="submit" class="btn">Найти</button>
    </form>
    <a class="btn btn-primary" href="${app}/clients/new">Добавить клиента</a>
</div>
<section class="card">
    <c:choose>
        <c:when test="${empty clients}"><p class="empty">Клиенты не найдены.</p></c:when>
        <c:otherwise>
            <div class="table-wrap">
                <table>
                    <thead><tr><th>ФИО</th><th>Дата рождения</th><th>Паспорт</th><th>Телефон</th><th>Регистрация</th><th></th></tr></thead>
                    <tbody>
                    <c:forEach var="client" items="${clients}">
                        <tr>
                            <td><a href="${app}/clients/view?id=${client.id}"><c:out value="${client.fullName}"/></a></td>
                            <td class="nowrap"><fmt:formatDate value="${client.birthDate}" pattern="dd.MM.yyyy"/></td>
                            <td class="nowrap"><c:out value="${client.passportNo}"/></td>
                            <td class="nowrap"><c:out value="${client.phone}"/></td>
                            <td class="nowrap"><fmt:formatDate value="${client.registeredOn}" pattern="dd.MM.yyyy"/></td>
                            <td class="row-actions">
                                <a class="btn btn-small" href="${app}/clients/edit?id=${client.id}">Изменить</a>
                                <form method="post" action="${app}/clients/delete" data-confirm="Удалить клиента вместе с его учётной записью?">
                                    <b:csrf/><input type="hidden" name="id" value="${client.id}">
                                    <button type="submit" class="btn btn-small btn-danger">Удалить</button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>
</section>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
