<c:set var="pageTitle" value="${currentUser.admin ? 'Кредиты' : 'Мои кредиты'}"/>
<%@ include file="/WEB-INF/jspf/header.jspf" %>
<div class="toolbar">
    <a class="btn btn-primary" href="${app}/loans/apply">Подать заявку</a>
    <a class="btn" href="${app}/reports/download?type=loans">Сохранить в файл</a>
</div>
<section class="card">
    <c:choose>
        <c:when test="${empty loans}"><p class="empty">Кредитов нет.</p></c:when>
        <c:otherwise>
            <div class="table-wrap">
                <table>
                    <thead>
                    <tr>
                        <th>№</th>
                        <b:hasRole role="ADMIN"><th>Клиент</th></b:hasRole>
                        <th>Счёт</th><th class="num">Сумма</th><th class="num">Ставка</th><th class="num">Срок</th>
                        <th class="num">Платёж в месяц</th><th class="num">Остаток долга</th><th>Статус</th><th></th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="loan" items="${loans}">
                        <tr>
                            <td>${loan.id}</td>
                            <b:hasRole role="ADMIN"><td><c:out value="${loan.clientName}"/></td></b:hasRole>
                            <td class="mono"><c:out value="${loan.accountNo}"/></td>
                            <td class="num nowrap"><b:money value="${loan.amount}" currency="${loan.currencyCode}"/></td>
                            <td class="num">${loan.annualRate}%</td>
                            <td class="num nowrap">${loan.termMonths} мес.</td>
                            <td class="num nowrap"><b:money value="${loan.monthlyPayment}" currency="${loan.currencyCode}"/></td>
                            <td class="num nowrap"><b:money value="${loan.remaining}" currency="${loan.currencyCode}"/></td>
                            <td><c:out value="${loan.statusTitle}"/></td>
                            <td class="row-actions">
                                <c:if test="${loan.hasStatus('ACTIVE')}">
                                    <form method="post" action="${app}/loans/pay" data-confirm="Списать платёж по кредиту со счёта?">
                                        <b:csrf/><input type="hidden" name="id" value="${loan.id}">
                                        <button type="submit" class="btn btn-small btn-primary">Внести платёж</button>
                                    </form>
                                </c:if>
                                <b:hasRole role="ADMIN">
                                    <c:if test="${loan.hasStatus('PENDING')}">
                                        <form method="post" action="${app}/loans/approve" data-confirm="Одобрить заявку и зачислить сумму на счёт клиента?">
                                            <b:csrf/><input type="hidden" name="id" value="${loan.id}">
                                            <button type="submit" class="btn btn-small btn-primary">Одобрить</button>
                                        </form>
                                        <form method="post" action="${app}/loans/reject" data-confirm="Отклонить заявку?">
                                            <b:csrf/><input type="hidden" name="id" value="${loan.id}">
                                            <button type="submit" class="btn btn-small">Отклонить</button>
                                        </form>
                                        <a class="btn btn-small" href="${app}/loans/edit?id=${loan.id}">Изменить</a>
                                    </c:if>
                                    <c:if test="${loan.hasStatus('PENDING') or loan.hasStatus('REJECTED')}">
                                        <form method="post" action="${app}/loans/delete" data-confirm="Удалить заявку?">
                                            <b:csrf/><input type="hidden" name="id" value="${loan.id}">
                                            <button type="submit" class="btn btn-small btn-danger">Удалить</button>
                                        </form>
                                    </c:if>
                                </b:hasRole>
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
