<c:set var="pageTitle" value="Пользователи"/>
<%@ include file="/WEB-INF/jspf/header.jspf" %>
<div class="toolbar">
    <a class="btn btn-primary" href="${app}/users/new">Добавить пользователя</a>
</div>
<section class="card">
    <div class="table-wrap">
        <table>
            <thead><tr><th>Логин</th><th>Роль</th><th>Клиент</th><th>Состояние</th><th></th></tr></thead>
            <tbody>
            <c:forEach var="u" items="${users}">
                <tr>
                    <td><c:out value="${u.login}"/></td>
                    <td><c:out value="${u.roleTitle}"/></td>
                    <td><c:out value="${u.clientName}" default="—"/></td>
                    <td><span class="badge ${u.active ? 'badge-on' : 'badge-off'}">${u.active ? 'Активен' : 'Заблокирован'}</span></td>
                    <td class="row-actions">
                        <a class="btn btn-small" href="${app}/users/edit?id=${u.id}">Изменить</a>
                        <c:if test="${u.id != currentUser.id}">
                            <form method="post" action="${app}/users/delete" data-confirm="Удалить учётную запись?">
                                <b:csrf/><input type="hidden" name="id" value="${u.id}">
                                <button type="submit" class="btn btn-small btn-danger">Удалить</button>
                            </form>
                        </c:if>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>
</section>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
