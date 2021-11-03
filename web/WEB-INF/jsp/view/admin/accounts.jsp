<%--
  Created by IntelliJ IDEA.
  User: ACER
  Date: 10/26/2021
  Time: 10:11 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Account list</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resource/style.css">
</head>
<body>
<c:import url="/WEB-INF/jsp/header.jspf"/>
<form action="findAccount" method="get">
    <input placeholder="EMAIL" type="text" name="name" value="${param.name}" />
    <input type="submit" value="Search by email" />
</form>
<c:url context="${pageContext.request.contextPath}/admin" value="/accounts" var="getAllURL"/>
<c:url context="${pageContext.request.contextPath}/admin" value="/accounts?enable=true" var="getEnableURL"/>
<c:url context="${pageContext.request.contextPath}/admin" value="/accounts?enable=false" var="getUnableURL"/>
<a href="${getAllURL}">Get all accounts</a>
<a href="${getEnableURL}">Get enable accounts</a>
<a href="${getUnableURL}">Get unable accounts</a>
<br>
<c:if test="${empty accountPage.content}">
    There's no account
</c:if>
<c:if test="${not empty accountPage.content}">
    <table>
        <thead>
            <th>Id</th>
            <th>Email</th>
            <th>Enable</th>
            <th>Authorities</th>
            <th></th>
        </thead>
        <tbody>
        <c:forEach items="${accountPage.content}" var="acc">
            <c:url context="${pageContext.request.contextPath}/admin" value="/switchAccountState?userId=${acc.id}" var="switchStateURL"/>
            <tr>
                <td>${acc.id}</td>
                <td>${acc.username}</td>
                <td>${acc.enabled}</td>
                <c:if test="${not empty acc.authorities}"><td>${acc.authorities.toArray()[0].authority}</td></c:if>
                <c:if test="${empty acc.authorities}"><td>No role</td></c:if>
                <td><a href="${switchStateURL}">Switch state</a></td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
    <p>
        Number of records: ${accountPage.totalElements}
        Current page: ${accountPage.number}
    </p>
    <c:if test="${accountPage.hasPreviousPage()}">
        <c:url context="${pageContext.request.contextPath}/admin" value="/accounts?enable=${param.enable}&page=${accountPage.number-1}" var="prePageURL" />
        <a href="${prePageURL}">Prev</a>
    </c:if>
    <c:if test="${accountPage.hasNextPage()}">
        <c:url context="${pageContext.request.contextPath}/admin" value="/accounts?enable=${param.enable}&page=${accountPage.number+1}" var="nextPageURL" />
        <a href="${nextPageURL}">Next</a>
    </c:if>
    <br>
    <c:url context="${pageContext.request.contextPath}/admin" value="/accounts?enable=${param.enable}&page=0" var="firstPage" />
    <a href="${firstPage}">First page</a>
    <c:url context="${pageContext.request.contextPath}/admin" value="/accounts?enable=${param.enable}&page=${accountPage.totalPages-1}" var="lastPage" />
    <a href="${lastPage}">Last page</a>
</c:if>
</body>
</html>
