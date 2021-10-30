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
    <input type="password" placeholder="PASSWORD" name="password">
    <input type="submit" value="SIGNUP">
</form>
<b>${signupMessage}</b>
</body>
</html>
