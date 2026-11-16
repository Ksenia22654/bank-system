<%@ page isErrorPage="true" %>
<c:set var="code" value="${pageContext.errorData.statusCode}"/>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Ошибка — Банк</title>
    <link rel="stylesheet" href="${ctx}/static/css/app.css">
</head>
<body class="centered">
<div class="card narrow">
    <c:choose>
        <c:when test="${code == 403}">
            <h1>Доступ запрещён</h1>
            <p>У вашей учётной записи нет прав на это действие.</p>
        </c:when>
        <c:when test="${code == 404}">
            <h1>Страница не найдена</h1>
            <p>Запрошенная страница или запись не существует.</p>
        </c:when>
        <c:otherwise>
            <h1>Ошибка сервера</h1>
            <p>Не удалось выполнить запрос. Попробуйте ещё раз; сведения об ошибке записаны в журнал сервера.</p>
        </c:otherwise>
    </c:choose>
    <a class="btn btn-primary" href="${app}/home">На главную</a>
</div>
</body>
</html>
