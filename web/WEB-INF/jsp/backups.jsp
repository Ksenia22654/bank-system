<c:set var="pageTitle" value="Резервные копии"/>
<%@ include file="/WEB-INF/jspf/header.jspf" %>
<div class="toolbar">
    <form method="post" action="${app}/backups/save">
        <b:csrf/>
        <button type="submit" class="btn btn-primary">Сохранить данные на сервере</button>
    </form>
</div>
<section class="card">
    <c:choose>
        <c:when test="${empty backups}"><p class="empty">Сохранённых копий пока нет.</p></c:when>
        <c:otherwise>
            <div class="table-wrap">
                <table>
                    <thead><tr><th>Файл</th><th>Создан</th><th class="num">Размер</th><th></th></tr></thead>
                    <tbody>
                    <c:forEach var="backup" items="${backups}">
                        <tr>
                            <td class="mono"><c:out value="${backup.fileName}"/></td>
                            <td class="nowrap"><fmt:formatDate value="${backup.createdAt}" pattern="dd.MM.yyyy HH:mm:ss"/></td>
                            <td class="num nowrap">${backup.sizeKb} КБ</td>
                            <td class="row-actions">
                                <form method="post" action="${app}/backups/restore" data-confirm="Заменить все текущие данные содержимым этой копии? Действие нельзя отменить.">
                                    <b:csrf/><input type="hidden" name="file" value="<c:out value='${backup.fileName}'/>">
                                    <button type="submit" class="btn btn-small btn-primary">Загрузить</button>
                                </form>
                                <form method="post" action="${app}/backups/delete" data-confirm="Удалить этот файл резервной копии?">
                                    <b:csrf/><input type="hidden" name="file" value="<c:out value='${backup.fileName}'/>">
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
