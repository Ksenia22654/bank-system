<c:set var="pageTitle" value="${client.persisted ? 'Изменение данных клиента' : 'Новый клиент'}"/>
<%@ include file="/WEB-INF/jspf/header.jspf" %>
<form class="card form" method="post" action="${app}/clients/save" data-validate novalidate>
    <b:csrf/>
    <input type="hidden" name="id" value="${client.id}">
    <div class="form-grid">
        <t:field name="lastName" label="Фамилия" value="${client.lastName}" required="true" maxlength="50"/>
        <t:field name="firstName" label="Имя" value="${client.firstName}" required="true" maxlength="50"/>
        <t:field name="middleName" label="Отчество" value="${client.middleName}" maxlength="50"/>
        <t:field name="birthDate" label="Дата рождения" type="date" value="${client.birthDate}" required="true" noFuture="true"/>
        <t:field name="passportNo" label="Паспорт" value="${client.passportNo}" required="true"
                 pattern="[0-9A-ZА-ЯЁ ]{6,20}" hint="От 6 до 20 цифр и заглавных букв" placeholder="4510 123456"/>
        <t:field name="phone" label="Телефон" type="tel" value="${client.phone}" required="true"
                 pattern="\+?[0-9 ()\-]{7,20}" hint="От 7 до 20 цифр, допустимы + ( ) -" placeholder="+7 900 111-22-33"/>
        <t:field name="email" label="Эл. почта" type="email" value="${client.email}" maxlength="100"/>
        <t:field name="address" label="Адрес" value="${client.address}" required="true" maxlength="200"/>
    </div>
    <div class="form-actions">
        <button type="submit" class="btn btn-primary">Сохранить</button>
        <a class="btn" href="${app}/clients">Отмена</a>
    </div>
</form>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
