<%@ tag pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ attribute name="name" required="true" %>
<%@ attribute name="label" required="true" %>
<%@ attribute name="value" %>
<%@ attribute name="type" %>
<%@ attribute name="required" type="java.lang.Boolean" %>
<%@ attribute name="pattern" %>
<%@ attribute name="hint" %>
<%@ attribute name="min" %>
<%@ attribute name="max" %>
<%@ attribute name="step" %>
<%@ attribute name="maxlength" %>
<%@ attribute name="placeholder" %>
<%@ attribute name="autocomplete" %>
<%@ attribute name="match" %>
<%@ attribute name="noFuture" type="java.lang.Boolean" %>
<c:set var="fieldError" value="${requestScope.errors[name]}"/>
<div class="field">
    <label for="f-${name}">${label}<c:if test="${required}"><span class="req" aria-hidden="true"> *</span></c:if></label>
    <input id="f-${name}" name="${name}" type="${empty type ? 'text' : type}" value="<c:out value='${value}'/>"
           <c:if test="${required}"> required </c:if>
           <c:if test="${not empty pattern}"> pattern="<c:out value='${pattern}'/>" </c:if>
           <c:if test="${not empty hint}"> title="<c:out value='${hint}'/>" </c:if>
           <c:if test="${not empty min}"> min="${min}" </c:if>
           <c:if test="${not empty max}"> max="${max}" </c:if>
           <c:if test="${not empty step}"> step="${step}" </c:if>
           <c:if test="${not empty maxlength}"> maxlength="${maxlength}" </c:if>
           <c:if test="${not empty placeholder}"> placeholder="<c:out value='${placeholder}'/>" </c:if>
           <c:if test="${not empty autocomplete}"> autocomplete="${autocomplete}" </c:if>
           <c:if test="${not empty match}"> data-match="${match}" </c:if>
           <c:if test="${noFuture}"> data-no-future="true" </c:if>
           <c:if test="${not empty fieldError}"> aria-invalid="true" </c:if>>
    <small class="field-error" data-for="${name}"><c:out value="${fieldError}"/></small>
    <c:if test="${not empty hint}"><small class="field-hint"><c:out value="${hint}"/></small></c:if>
</div>
