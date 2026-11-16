<c:set var="pageTitle" value="${user.persisted ? 'Изменение пользователя' : 'Новый пользователь'}"/>
<%@ include file="/WEB-INF/jspf/header.jspf" %>
<form class="card form" method="post" action="${app}/users/save" data-validate novalidate>
    <b:csrf/>
    <input type="hidden" name="id" value="${user.id}">
    <t:field name="login" label="Логин" value="${user.login}" required="true" pattern="[A-Za-z0-9_.\-]{3,30}"
             hint="От 3 до 30 латинских букв, цифр и знаков _ . -" autocomplete="off"/>
    <t:field name="password" label="${user.persisted ? 'Новый пароль (если нужно сменить)' : 'Пароль'}" type="password"
             required="${not user.persisted}" pattern=".{6,100}" hint="Не короче 6 символов" autocomplete="new-password"/>
    <t:select name="roleCode" label="Роль" required="true">
        <option value="USER" ${user.roleCode == 'USER' ? 'selected' : ''}>Пользователь</option>
        <option value="ADMIN" ${user.roleCode == 'ADMIN' ? 'selected' : ''}>Администратор</option>
    </t:select>
    <t:select name="clientId" label="Клиент (для роли «Пользователь»)">
        <option value="">Не выбран</option>
        <c:forEach var="client" items="${clients}">
            <option value="${client.id}" ${client.id == user.clientId ? 'selected' : ''}><c:out value="${client.fullName}"/></option>
        </c:forEach>
    </t:select>
    <div class="field field-check">
        <label><input type="checkbox" name="active" ${user.active ? 'checked' : ''}> Учётная запись активна</label>
    </div>
    <div class="form-actions">
        <button type="submit" class="btn btn-primary">Сохранить</button>
        <a class="btn" href="${app}/users">Отмена</a>
    </div>
</form>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
