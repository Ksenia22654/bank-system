<%@ tag pageEncoding="UTF-8" trimDirectiveWhitespaces="true" body-content="scriptless" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ attribute name="name" required="true" %>
<%@ attribute name="label" required="true" %>
<%@ attribute name="required" type="java.lang.Boolean" %>
<c:set var="fieldError" value="${requestScope.errors[name]}"/>
<div class="field">
    <label for="f-${name}">${label}<c:if test="${required}"><span class="req" aria-hidden="true"> *</span></c:if></label>
    <select id="f-${name}" name="${name}"<c:if test="${required}"> required</c:if><c:if test="${not empty fieldError}"> aria-invalid="true"</c:if>>
        <jsp:doBody/>
    </select>
    <small class="field-error" data-for="${name}"><c:out value="${fieldError}"/></small>
</div>
