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
    <title>Category list</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resource/style.css">
</head>
<body>
<c:import url="/WEB-INF/jsp/header.jspf"/>
<form action="addCategory" method="get">
    <input placeholder="CATEGORY NAME" type="text" name="categoryName" />
    <input type="submit" value="ADD NEW CATEGORY" />
</form>
<c:url context="${pageContext.request.contextPath}/admin" value="/categories" var="getAllURL"/>
<a href="${getAllURL}">Get all categories</a>
<br>
<b>${addCategoryError}</b>
<c:if test="${empty categories}">
    There's no category
</c:if>
<c:if test="${not empty categories}">
    <table>
        <thead>
            <th>Id</th>
            <th>Category name</th>
            <th>Enable</th>
            <th></th>
        </thead>
        <tbody>
        <c:forEach items="${categories}" var="cat">
            <c:url context="${pageContext.request.contextPath}/admin" value="/switchCategoryState?categoryId=${cat.id}" var="switchStateURL"/>
            <tr>
                <td>${cat.id}</td>
                <td>${cat.name}</td>
                <td>${cat.status}</td>
                <td><a href="${switchStateURL}">Switch state</a></td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</c:if>
</body>
</html>
