<%--
  Created by IntelliJ IDEA.
  User: ACER
  Date: 10/28/2021
  Time: 12:53 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Signup</title>
</head>
<body>

<form method="post" action="signup">
    <input type="text" placeholder="USERNAME" name="username">
    <br>
    <input type="password" placeholder="PASSWORD" name="password">
    <br>
    <input type="submit" value="SIGNUP">
</form>
<b>${signupMessage}</b>
<c:url context="${pageContext.request.contextPath}/admin" value="/login" var="loginURL"/>
<a href="${loginURL}">Log in</a>
</body>
</html>
