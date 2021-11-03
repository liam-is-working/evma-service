<%--
  Created by IntelliJ IDEA.
  User: ACER
  Date: 10/26/2021
  Time: 6:10 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Login</title>
</head>
<body>
<%--</c:if><c:if test="${param.containsKey('loggedOut')}">--%>
<%--    <i>Logout</i><br /><br />--%>
<%--</c:if>--%>

<form:form method="post" modelAttribute="loginForm" autocomplete="off">
    <form:label path="username">Username</form:label><br />
    <form:input path="username" autocomplete="off" /><br />
    <form:label path="password">Password</form:label><br />
    <form:password path="password" autocomplete="off" /><br />
    <input type="submit" value="Log in" />
</form:form>
<c:if test="${param.containsKey('loginFailed')}">
    <b> Login failed</b>
</c:if>
    <br />
<c:url context="${pageContext.request.contextPath}" value="/signup" var="signupURL"/>
<a href="${signupURL}">Signup</a>
</body>
</html>
