<c:set var="pageTitle" value="Смена пароля"/>
<%@ include file="/WEB-INF/jspf/header.jspf" %>
<form class="card form" method="post" action="${app}/profile" data-validate novalidate>
    <b:csrf/>
    <t:field name="current" label="Текущий пароль" type="password" required="true" autocomplete="current-password"/>
    <t:field name="next" label="Новый пароль" type="password" required="true" pattern=".{6,100}" hint="Не короче 6 символов" autocomplete="new-password"/>
    <t:field name="repeat" label="Повторите новый пароль" type="password" required="true" match="next" autocomplete="new-password"/>
    <div class="form-actions"><button type="submit" class="btn btn-primary">Сохранить</button></div>
</form>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
