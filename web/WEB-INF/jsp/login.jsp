<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Вход — Банк</title>
    <link rel="stylesheet" href="${ctx}/static/css/app.css">
    <script src="${ctx}/static/js/app.js" defer></script>
</head>
<body class="centered">
<div class="card narrow">
    <h1>Вход в систему</h1>
    <p class="muted">Автоматизация работы банка с физическими лицами</p>
    <c:if test="${not empty sessionScope.flash}">
        <div class="alert alert-${sessionScope.flash.kind}" role="status"><c:out value="${sessionScope.flash.text}"/></div>
        <c:remove var="flash" scope="session"/>
    </c:if>
    <c:if test="${not empty loginError}">
        <div class="alert alert-error" role="alert"><c:out value="${loginError}"/></div>
    </c:if>
    <form method="post" action="${app}/login" data-validate novalidate>
        <b:csrf/>
        <t:field name="login" label="Логин" value="${enteredLogin}" required="true" autocomplete="username"/>
        <t:field name="password" label="Пароль" type="password" required="true" autocomplete="current-password"/>
        <button type="submit" class="btn btn-primary btn-wide">Войти</button>
    </form>
</div>
</body>
</html>
